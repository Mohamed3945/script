import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ParameterDefinition } from '../../../../core/models/parameter-definition.model';
import { ParameterDefinitionApiService } from '../../../../core/services/parameter-definition-api.service';
import { ParameterDefinitionTableComponent } from '../../components/parameter-definitions/parameter-definition-table/parameter-definition-table.component';

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

  constructor(
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDefinitions();
  }

  loadDefinitions(): void {
    this.loading$.next(true);
    this.parameterDefinitionApiService.getDefinitions().subscribe({
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

