import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ChamberCapability } from '../../../../../core/models/chamber-capability.model';

@Component({
  selector: 'app-chamber-capability-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './chamber-capability-table.component.html',
  styleUrl: './chamber-capability-table.component.scss'
})
export class ChamberCapabilityTableComponent {
  @Input() capabilities: ChamberCapability[] = [];
  @Input() mode: 'catalog' | 'assigned' = 'catalog';

  @Output() edit = new EventEmitter<ChamberCapability>();
  @Output() delete = new EventEmitter<ChamberCapability>();
  @Output() remove = new EventEmitter<ChamberCapability>();

  readonly pageSize = 15;
  filterText = '';
  currentPage = 1;

  onFilterChange(value: string): void {
    this.filterText = value;
    this.currentPage = 1;
  }

  get filteredCapabilities(): ChamberCapability[] {
    const query = this.filterText.trim().toLowerCase();
    if (!query) {
      return this.capabilities;
    }

    return this.capabilities.filter((capability) => JSON.stringify(capability).toLowerCase().includes(query));
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredCapabilities.length / this.pageSize));
  }

  get pagedCapabilities(): ChamberCapability[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredCapabilities.slice(start, start + this.pageSize);
  }

  previousPage(): void {
    this.currentPage = Math.max(1, this.currentPage - 1);
  }

  nextPage(): void {
    this.currentPage = Math.min(this.totalPages, this.currentPage + 1);
  }
}
