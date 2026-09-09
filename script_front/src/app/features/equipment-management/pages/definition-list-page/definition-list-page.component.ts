import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ConfigurationDefinition } from '../../../../core/models/configuration-definition.model';
import { ConfigurationDefinitionApiService } from '../../../../core/services/configuration-definition-api.service';
import { DefinitionTableComponent } from '../../components/definitions/definition-table/definition-table.component';

@Component({
  selector: 'app-definition-list-page',
  standalone: true,
  imports: [AsyncPipe, NgIf, DefinitionTableComponent],
  templateUrl: './definition-list-page.component.html',
  styleUrl: './definition-list-page.component.scss'
})
export class DefinitionListPageComponent implements OnInit {
  definitions$ = new BehaviorSubject<ConfigurationDefinition[]>([]);

  constructor(
    private router: Router,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService
  ) {}

  ngOnInit(): void {
    this.loadDefinitions();
  }

  loadDefinitions(): void {
    this.configurationDefinitionApiService.getDefinitions().subscribe({
      next: (definitions) => this.definitions$.next(definitions),
      error: (error) => console.error('Failed to load definitions', error)
    });
  }

  createDefinition(): void {
    this.router.navigate(['/reference-data/configuration-definitions/new']);
  }

  editDefinition(definition: ConfigurationDefinition): void {
    if (!definition.id) return;
    this.router.navigate(['/reference-data/configuration-definitions', definition.id, 'edit']);
  }

  deleteDefinition(definition: ConfigurationDefinition): void {
    if (!definition.id) return;
    const confirmed = window.confirm(`Delete definition "${definition.code}"?`);
    if (!confirmed) return;

    this.configurationDefinitionApiService.deleteDefinition(definition.id).subscribe({
      next: () => this.loadDefinitions(),
      error: (error) => console.error('Failed to delete definition', error)
    });
  }
}
