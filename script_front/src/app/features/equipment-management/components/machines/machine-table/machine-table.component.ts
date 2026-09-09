import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Machine } from '../../../../../core/models/machine.model';

@Component({
  selector: 'app-machine-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './machine-table.component.html',
  styleUrl: './machine-table.component.scss'
})
export class MachineTableComponent {
  @Input() machines: Machine[] = [];

  @Output() view = new EventEmitter<Machine>();
  @Output() edit = new EventEmitter<Machine>();
  @Output() delete = new EventEmitter<Machine>();

  readonly pageSize = 15;
  filterText = '';
  currentPage = 1;

  onFilterChange(value: string): void {
    this.filterText = value;
    this.currentPage = 1;
  }

  get filteredMachines(): Machine[] {
    const query = this.filterText.trim().toLowerCase();
    if (!query) {
      return this.machines;
    }

    return this.machines.filter((machine) => JSON.stringify(machine).toLowerCase().includes(query));
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredMachines.length / this.pageSize));
  }

  get pagedMachines(): Machine[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredMachines.slice(start, start + this.pageSize);
  }

  previousPage(): void {
    this.currentPage = Math.max(1, this.currentPage - 1);
  }

  nextPage(): void {
    this.currentPage = Math.min(this.totalPages, this.currentPage + 1);
  }
}
