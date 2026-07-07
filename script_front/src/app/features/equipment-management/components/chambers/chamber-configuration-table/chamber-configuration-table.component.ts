import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ChamberConfiguration } from '../../../../../core/models/chamber-configuration.model';

@Component({
  selector: 'app-chamber-configuration-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './chamber-configuration-table.component.html',
  styleUrl: './chamber-configuration-table.component.scss'
})
export class ChamberConfigurationTableComponent {
  @Input() configurations: ChamberConfiguration[] = [];

  @Output() edit = new EventEmitter<ChamberConfiguration>();
  @Output() delete = new EventEmitter<ChamberConfiguration>();

  readonly pageSize = 15;
  filterText = '';
  currentPage = 1;

  onFilterChange(value: string): void {
    this.filterText = value;
    this.currentPage = 1;
  }

  get filteredConfigurations(): ChamberConfiguration[] {
    const query = this.filterText.trim().toLowerCase();
    if (!query) {
      return this.configurations;
    }

    return this.configurations.filter((configuration) => JSON.stringify(configuration).toLowerCase().includes(query));
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredConfigurations.length / this.pageSize));
  }

  get pagedConfigurations(): ChamberConfiguration[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredConfigurations.slice(start, start + this.pageSize);
  }

  previousPage(): void {
    this.currentPage = Math.max(1, this.currentPage - 1);
  }

  nextPage(): void {
    this.currentPage = Math.min(this.totalPages, this.currentPage + 1);
  }
}
