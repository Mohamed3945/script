import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ParameterDefinition } from '../../../../core/models/parameter-definition.model';
import { StepType } from '../../../../core/models/step-type.model';
import { ParameterDefinitionApiService } from '../../../../core/services/parameter-definition-api.service';
import { ParameterDefinitionTableComponent } from '../../components/parameter-definitions/parameter-definition-table/parameter-definition-table.component';

type ParameterDefinitionFilter = 'ALL' | StepType;

@Component({
  selector: 'app-parameter-definition-list-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, ParameterDefinitionTableComponent],
  templateUrl: './parameter-definition-list-page.component.html',
  styleUrl: './parameter-definition-list-page.component.scss'
})
export class ParameterDefinitionListPageComponent implements OnInit {
  definitions$ = new BehaviorSubject<ParameterDefinition[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  readonly availableFilters: ParameterDefinitionFilter[] = ['ALL', 'STEP', 'PRESTEP'];
  selectedFilter: ParameterDefinitionFilter = 'ALL';

  constructor(
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDefinitions();
  }

  loadDefinitions(): void {
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

  onCreate(): void {
    this.router.navigate(['/recipes/parameter/new']);
  }

  onView(definition: ParameterDefinition): void {
    if (!definition.id) return;
    this.router.navigate(['/recipes/parameter', definition.id]);
  }

  onEdit(definition: ParameterDefinition): void {
    if (!definition.id) return;
    this.router.navigate(['/recipes/parameter', definition.id, 'edit']);
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

