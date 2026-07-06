import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ParameterDependencyRuleView } from '../../../../core/models/parameter-dependency-rule-view.model';
import { ParameterDependencyRuleApiService } from '../../../../core/services/parameter-dependency-rule-api.service';
import { DependencyRuleTableComponent } from '../../components/dependency-rules/dependency-rule-table/dependency-rule-table.component';

@Component({
  selector: 'app-dependency-rule-list-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, DependencyRuleTableComponent],
  templateUrl: './dependency-rule-list-page.component.html',
  styleUrl: './dependency-rule-list-page.component.scss'
})
/**
 * DependencyRuleListPageComponent coordinates UI logic for this feature.
 */
export class DependencyRuleListPageComponent implements OnInit {
  rules$ = new BehaviorSubject<ParameterDependencyRuleView[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);

  constructor(
    private parameterDependencyRuleApiService: ParameterDependencyRuleApiService,
    private router: Router
  ) {}

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    this.loadRules();
  }

  /**
   * Handles the loadRules workflow.
   */
  loadRules(): void {
    this.loading$.next(true);

    this.parameterDependencyRuleApiService.getRules().subscribe({
      next: (rules) => {
        this.rules$.next(rules);
        this.loading$.next(false);
      },
      error: (error) => {
        console.error('Failed to load dependency rules', error);
        this.rules$.next([]);
        this.loading$.next(false);
      }
    });
  }

  /**
   * Handles the onCreate workflow.
   */
  onCreate(): void {
    this.router.navigate(['/recipes/rules/new']);
  }

  /**
   * Handles the onView workflow.
   */
  onView(rule: ParameterDependencyRuleView): void {
    this.router.navigate(['/recipes/rules', rule.id]);
  }

  /**
   * Handles the onEdit workflow.
   */
  onEdit(rule: ParameterDependencyRuleView): void {
    this.router.navigate(['/recipes/rules', rule.id, 'edit']);
  }

  /**
   * Handles the onDelete workflow.
   */
  onDelete(rule: ParameterDependencyRuleView): void {
    const confirmed = window.confirm(
      `Delete dependency rule ${rule.sourceDefinitionCode || '-'} -> ${rule.targetDefinitionCode || '-'}?`
    );
    if (!confirmed) {
      return;
    }

    this.parameterDependencyRuleApiService.deleteRule(rule.id).subscribe({
      next: () => this.loadRules(),
      error: (error) => console.error('Failed to delete dependency rule', error)
    });
  }
}

