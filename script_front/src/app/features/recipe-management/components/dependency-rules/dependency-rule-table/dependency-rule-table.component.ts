import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ParameterDependencyRuleView } from '../../../../../core/models/parameter-dependency-rule-view.model';

@Component({
  selector: 'app-dependency-rule-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './dependency-rule-table.component.html',
  styleUrl: './dependency-rule-table.component.scss'
})
/**
 * DependencyRuleTableComponent coordinates UI logic for this feature.
 */
export class DependencyRuleTableComponent {
  @Input() rules: ParameterDependencyRuleView[] = [];

  @Output() viewRule = new EventEmitter<ParameterDependencyRuleView>();
  @Output() editRule = new EventEmitter<ParameterDependencyRuleView>();
  @Output() deleteRule = new EventEmitter<ParameterDependencyRuleView>();

  readonly pageSize = 15;
  filterText = '';
  currentPage = 1;

  onFilterChange(value: string): void {
    this.filterText = value;
    this.currentPage = 1;
  }

  get filteredRules(): ParameterDependencyRuleView[] {
    const query = this.filterText.trim().toLowerCase();
    if (!query) {
      return this.rules;
    }

    return this.rules.filter((rule) =>
      JSON.stringify(rule).toLowerCase().includes(query)
    );
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredRules.length / this.pageSize));
  }

  get pagedRules(): ParameterDependencyRuleView[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredRules.slice(start, start + this.pageSize);
  }

  previousPage(): void {
    this.currentPage = Math.max(1, this.currentPage - 1);
  }

  nextPage(): void {
    this.currentPage = Math.min(this.totalPages, this.currentPage + 1);
  }
}

