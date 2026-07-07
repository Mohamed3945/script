import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';

@Component({
  selector: 'app-parameter-definition-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './parameter-definition-table.component.html',
  styleUrl: './parameter-definition-table.component.scss'
})
/**
 * ParameterDefinitionTableComponent coordinates UI logic for this feature.
 */
export class ParameterDefinitionTableComponent {
  @Input() definitions: ParameterDefinition[] = [];

  @Output() viewDefinition = new EventEmitter<ParameterDefinition>();
  @Output() editDefinition = new EventEmitter<ParameterDefinition>();
  @Output() deleteDefinition = new EventEmitter<ParameterDefinition>();

  readonly pageSize = 15;
  filterText = '';
  currentPage = 1;

  onFilterChange(value: string): void {
    this.filterText = value;
    this.currentPage = 1;
  }

  get filteredDefinitions(): ParameterDefinition[] {
    const query = this.filterText.trim().toLowerCase();
    if (!query) {
      return this.definitions;
    }

    return this.definitions.filter((definition) => JSON.stringify(definition).toLowerCase().includes(query));
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredDefinitions.length / this.pageSize));
  }

  get pagedDefinitions(): ParameterDefinition[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredDefinitions.slice(start, start + this.pageSize);
  }

  previousPage(): void {
    this.currentPage = Math.max(1, this.currentPage - 1);
  }

  nextPage(): void {
    this.currentPage = Math.min(this.totalPages, this.currentPage + 1);
  }
}

