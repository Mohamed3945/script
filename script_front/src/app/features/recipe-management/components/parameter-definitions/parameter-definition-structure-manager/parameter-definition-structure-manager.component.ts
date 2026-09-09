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
  draggingDefinitionId: number | null = null;
  draggingFromGroupId: number | null = null;

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
    this.persistDefinitionOrder(groupVm, reordered);
  }

  moveDefinitionDown(groupVm: ParameterDefinitionGroupVm, index: number): void {
    if (index >= groupVm.definitions.length - 1 || !groupVm.group.id) {
      return;
    }

    const reordered = [...groupVm.definitions];
    [reordered[index], reordered[index + 1]] = [reordered[index + 1], reordered[index]];
    this.persistDefinitionOrder(groupVm, reordered);
  }

  onDefinitionDragStart(groupVm: ParameterDefinitionGroupVm, definition: ParameterDefinition): void {
    if (!groupVm.group.id || !definition.id) {
      return;
    }

    this.draggingDefinitionId = definition.id;
    this.draggingFromGroupId = groupVm.group.id;
  }

  onDefinitionDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  onDefinitionDrop(groupVm: ParameterDefinitionGroupVm, targetIndex: number): void {
    if (!groupVm.group.id || !this.draggingDefinitionId || !this.draggingFromGroupId) {
      this.resetDragState();
      return;
    }

    if (this.draggingFromGroupId !== groupVm.group.id) {
      this.resetDragState();
      return;
    }

    const sourceIndex = groupVm.definitions.findIndex((definition) => definition.id === this.draggingDefinitionId);
    if (sourceIndex < 0 || sourceIndex === targetIndex) {
      this.resetDragState();
      return;
    }

    const reordered = [...groupVm.definitions];
    const [moved] = reordered.splice(sourceIndex, 1);
    reordered.splice(targetIndex, 0, moved);

    this.persistDefinitionOrder(groupVm, reordered);
    this.resetDragState();
  }

  onDefinitionDragEnd(): void {
    this.resetDragState();
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

    const previousGroups = this.snapshotGroups();

    const updatedGroups = this.groups.map((groupVm) => {
      if (groupVm.group.id === currentGroup.group.id) {
        return {
          ...groupVm,
          definitions: groupVm.definitions.filter((d) => d.id !== definition.id)
        };
      }

      if (groupVm.group.id === targetGroup.group.id) {
        return {
          ...groupVm,
          definitions: [...groupVm.definitions, definition]
        };
      }

      return groupVm;
    });

    this.groups = updatedGroups;

    this.parameterDefinitionApiService.moveDefinition(definition.id, targetGroup.group.id, targetIndex).subscribe({
      next: () => {
        if (definition.id) {
          this.moveTargetsByDefinitionId[definition.id] = null;
        }
      },
      error: (error) => {
        console.error('Failed to move definition to another group', error);
        this.groups = previousGroups;
      }
    });
  }

  availableTargetGroups(currentGroupId?: number | null): ParameterGroup[] {
    return this.groups
      .map(groupVm => groupVm.group)
      .filter(group => group.id != null && group.id !== currentGroupId);
  }

  private persistGroupOrder(reordered: ParameterDefinitionGroupVm[]): void {
    const previousGroups = this.snapshotGroups();
    this.groups = reordered;

    const orderedGroupIds = reordered
      .map(item => item.group.id)
      .filter((id): id is number => id != null);

    this.parameterGroupApiService.reorderGroups(this.stepType, orderedGroupIds).subscribe({
      next: () => {},
      error: (error) => {
        console.error('Failed to reorder parameter groups', error);
        this.groups = previousGroups;
      }
    });
  }

  private persistDefinitionOrder(groupVm: ParameterDefinitionGroupVm, reorderedDefinitions: ParameterDefinition[]): void {
    if (!groupVm.group.id) {
      return;
    }

    const previousGroups = this.snapshotGroups();

    this.groups = this.groups.map((candidate) => {
      if (candidate.group.id !== groupVm.group.id) {
        return candidate;
      }

      return {
        ...candidate,
        definitions: [...reorderedDefinitions]
      };
    });

    const orderedDefinitionIds = reorderedDefinitions
      .map(def => def.id)
      .filter((id): id is number => id != null);

    this.parameterDefinitionApiService.reorderDefinitions(groupVm.group.id, orderedDefinitionIds).subscribe({
      next: () => {},
      error: (error) => {
        console.error('Failed to reorder definitions', error);
        this.groups = previousGroups;
      }
    });
  }

  private snapshotGroups(): ParameterDefinitionGroupVm[] {
    return this.groups.map((groupVm) => ({
      group: groupVm.group,
      definitions: [...groupVm.definitions]
    }));
  }

  private resetDragState(): void {
    this.draggingDefinitionId = null;
    this.draggingFromGroupId = null;
  }
}