import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ConfigurationDefinition } from '../../../../../core/models/configuration-definition.model';

@Component({
  selector: 'app-definition-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './definition-table.component.html',
  styleUrl: './definition-table.component.scss'
})
export class DefinitionTableComponent {
  @Input() definitions: ConfigurationDefinition[] = [];
  @Output() edit = new EventEmitter<ConfigurationDefinition>();
  @Output() delete = new EventEmitter<ConfigurationDefinition>();

  readonly pageSize = 15;
  filterText = '';
  currentPage = 1;

  onFilterChange(value: string): void {
    this.filterText = value;
    this.currentPage = 1;
  }

  get filteredDefinitions(): ConfigurationDefinition[] {
    const query = this.filterText.trim().toLowerCase();
    if (!query) {
      return this.definitions;
    }

    return this.definitions.filter((definition) => JSON.stringify(definition).toLowerCase().includes(query));
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredDefinitions.length / this.pageSize));
  }

  get pagedDefinitions(): ConfigurationDefinition[] {
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
