import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ParameterGroup } from '../../../../../core/models/parameter-group.model';
import { ParameterScope } from '../../../../../core/models/parameter-scope.model';
import { ParameterDefinitionApiService } from '../../../../../core/services/parameter-definition-api.service';
import { ParameterGroupApiService } from '../../../../../core/services/parameter-group-api.service';

interface ParameterDefinitionGroupVm {
  group: ParameterGroup;
  definitions: ParameterDefinition[];
}

@Component({
  selector: 'app-parameter-definition-structure-manager',
  standalone: true,
  imports: [NgIf, NgFor, FormsModule],
  templateUrl: './parameter-definition-structure-manager.component.html',
  styleUrl: './parameter-definition-structure-manager.component.scss'
})
export class ParameterDefinitionStructureManagerComponent implements OnInit {
  stepType: ParameterScope = 'STEP';
  groups: ParameterDefinitionGroupVm[] = [];
  newGroupName = '';
  loading = false;

  moveTargetsByDefinitionId: Record<number, number | null> = {};

  constructor(
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private parameterGroupApiService: ParameterGroupApiService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  onStepTypeChanged(stepType: ParameterScope): void {
    this.stepType = stepType;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.moveTargetsByDefinitionId = {};

    this.parameterGroupApiService.getGroups(this.stepType).subscribe({
      next: (groups) => {
        this.parameterDefinitionApiService.getDefinitions(this.stepType).subscribe({
          next: (definitions) => {
            this.groups = groups
              .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0))
              .map(group => ({
                group,
                definitions: definitions
                  .filter(def => def.parameterGroupId === group.id)
                  .sort((a, b) => (a.orderIndexInGroup ?? 0) - (b.orderIndexInGroup ?? 0))
              }));

            this.loading = false;
          },
          error: (error) => {
            console.error('Failed to load parameter definitions', error);
            this.groups = [];
            this.loading = false;
          }
        });
      },
      error: (error) => {
        console.error('Failed to load parameter groups', error);
        this.groups = [];
        this.loading = false;
      }
    });
  }

  onCreateGroup(): void {
    const name = this.newGroupName.trim();
    if (!name) {
      return;
    }

    this.parameterGroupApiService.createGroup({
      name,
      stepType: this.stepType
    }).subscribe({
      next: () => {
        this.newGroupName = '';
        this.load();
      },
      error: (error) => {
        console.error('Failed to create parameter group', error);
      }
    });
  }

  moveGroupUp(index: number): void {
    if (index <= 0) {
      return;
    }

    const reordered = [...this.groups];
    [reordered[index - 1], reordered[index]] = [reordered[index], reordered[index - 1]];
    this.persistGroupOrder(reordered);
  }

  moveGroupDown(index: number): void {
    if (index >= this.groups.length - 1) {
      return;
    }

    const reordered = [...this.groups];
    [reordered[index], reordered[index + 1]] = [reordered[index + 1], reordered[index]];
    this.persistGroupOrder(reordered);
  }

  moveDefinitionUp(groupVm: ParameterDefinitionGroupVm, index: number): void {
    if (index <= 0 || !groupVm.group.id) {
      return;
    }

    const reordered = [...groupVm.definitions];
    [reordered[index - 1], reordered[index]] = [reordered[index], reordered[index - 1]];

    const orderedDefinitionIds = reordered
      .map(def => def.id)
      .filter((id): id is number => id != null);

    this.parameterDefinitionApiService.reorderDefinitions(groupVm.group.id, orderedDefinitionIds).subscribe({
      next: () => this.load(),
      error: (error) => {
        console.error('Failed to reorder definitions', error);
        this.load();
      }
    });
  }

  moveDefinitionDown(groupVm: ParameterDefinitionGroupVm, index: number): void {
    if (index >= groupVm.definitions.length - 1 || !groupVm.group.id) {
      return;
    }

    const reordered = [...groupVm.definitions];
    [reordered[index], reordered[index + 1]] = [reordered[index + 1], reordered[index]];

    const orderedDefinitionIds = reordered
      .map(def => def.id)
      .filter((id): id is number => id != null);

    this.parameterDefinitionApiService.reorderDefinitions(groupVm.group.id, orderedDefinitionIds).subscribe({
      next: () => this.load(),
      error: (error) => {
        console.error('Failed to reorder definitions', error);
        this.load();
      }
    });
  }

  moveDefinitionToGroup(definition: ParameterDefinition, currentGroup: ParameterDefinitionGroupVm): void {
    if (!definition.id) {
      return;
    }

    const targetGroupId = this.moveTargetsByDefinitionId[definition.id];
    if (!targetGroupId || targetGroupId === currentGroup.group.id) {
      return;
    }

    const targetGroup = this.groups.find(groupVm => groupVm.group.id === targetGroupId);
    if (!targetGroup?.group.id) {
      return;
    }

    const targetIndex = targetGroup.definitions.length;

    this.parameterDefinitionApiService.moveDefinition(definition.id, targetGroup.group.id, targetIndex).subscribe({
      next: () => this.load(),
      error: (error) => {
        console.error('Failed to move definition to another group', error);
        this.load();
      }
    });
  }

  availableTargetGroups(currentGroupId?: number | null): ParameterGroup[] {
    return this.groups
      .map(groupVm => groupVm.group)
      .filter(group => group.id != null && group.id !== currentGroupId);
  }

  private persistGroupOrder(reordered: ParameterDefinitionGroupVm[]): void {
    const orderedGroupIds = reordered
      .map(item => item.group.id)
      .filter((id): id is number => id != null);

    this.parameterGroupApiService.reorderGroups(this.stepType, orderedGroupIds).subscribe({
      next: () => this.load(),
      error: (error) => {
        console.error('Failed to reorder parameter groups', error);
        this.load();
      }
    });
  }
}