import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ParameterDefinition } from '../../../../core/models/parameter-definition.model';
import { StepType } from '../../../../core/models/step-type.model';
import { ParameterDefinitionApiService } from '../../../../core/services/parameter-definition-api.service';
import { ParameterDefinitionStructureManagerComponent } from '../../components/parameter-definitions/parameter-definition-structure-manager/parameter-definition-structure-manager.component';
import { ParameterDefinitionTableComponent } from '../../components/parameter-definitions/parameter-definition-table/parameter-definition-table.component';

type ParameterDefinitionFilter = 'ALL' | StepType;
type ParameterDefinitionViewMode = 'table' | 'structure';

@Component({
  selector: 'app-parameter-definition-list-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, ParameterDefinitionTableComponent, ParameterDefinitionStructureManagerComponent],
  templateUrl: './parameter-definition-list-page.component.html',
  styleUrl: './parameter-definition-list-page.component.scss'
})
export class ParameterDefinitionListPageComponent implements OnInit {
  definitions$ = new BehaviorSubject<ParameterDefinition[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  readonly availableFilters: ParameterDefinitionFilter[] = ['ALL', 'STEP', 'PRESTEP'];
  selectedFilter: ParameterDefinitionFilter = 'ALL';
  viewMode: ParameterDefinitionViewMode = 'table';

  constructor(
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDefinitions();
  }

  loadDefinitions(): void {
    if (this.viewMode === 'structure') {
      return;
    }

    this.loading$.next(true);
    const stepType = this.selectedFilter === 'ALL' ? undefined : this.selectedFilter;

    this.parameterDefinitionApiService.getDefinitions(stepType).subscribe({
      next: (definitions) => {
        this.definitions$.next(definitions);
        this.loading$.next(false);
      },
      error: (error) => {
        console.error('Failed to load parameter definitions', error);
        this.definitions$.next([]);
        this.loading$.next(false);
      }
    });
  }

  onFilterChanged(rawValue: string): void {
    if (rawValue !== 'ALL' && rawValue !== 'STEP' && rawValue !== 'PRESTEP') {
      return;
    }

    this.selectedFilter = rawValue;
    this.loadDefinitions();
  }

  onViewModeChanged(rawValue: string): void {
    if (rawValue !== 'table' && rawValue !== 'structure') {
      return;
    }

    this.viewMode = rawValue;
    if (this.viewMode === 'table') {
      this.loadDefinitions();
    }
  }

  onCreate(): void {
    this.router.navigate(['/parameters/new']);
  }

  onView(definition: ParameterDefinition): void {
    if (!definition.id) return;
    this.router.navigate(['/parameters', definition.id]);
  }

  onEdit(definition: ParameterDefinition): void {
    if (!definition.id) return;
    this.router.navigate(['/parameters', definition.id, 'edit']);
  }

  onDelete(definition: ParameterDefinition): void {
    if (!definition.id) return;

    const confirmed = window.confirm(`Delete parameter definition "${definition.name}"?`);
    if (!confirmed) return;

    this.parameterDefinitionApiService.deleteDefinition(definition.id).subscribe({
      next: () => this.loadDefinitions(),
      error: (error) => console.error('Failed to delete parameter definition', error)
    });
  }
}