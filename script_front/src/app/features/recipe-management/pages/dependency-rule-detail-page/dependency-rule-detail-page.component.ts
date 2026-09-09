import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ParameterDependencyRuleView } from '../../../../core/models/parameter-dependency-rule-view.model';
import { ParameterDependencyRuleApiService } from '../../../../core/services/parameter-dependency-rule-api.service';
import { DependencyRuleSummaryComponent } from '../../components/dependency-rules/dependency-rule-summary/dependency-rule-summary.component';

@Component({
  selector: 'app-dependency-rule-detail-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, DependencyRuleSummaryComponent],
  templateUrl: './dependency-rule-detail-page.component.html',
  styleUrl: './dependency-rule-detail-page.component.scss'
})
/**
 * DependencyRuleDetailPageComponent coordinates UI logic for this feature.
 */
export class DependencyRuleDetailPageComponent implements OnInit {
  rule$ = new BehaviorSubject<ParameterDependencyRuleView | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private parameterDependencyRuleApiService: ParameterDependencyRuleApiService
  ) {}

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      return;
    }

    this.parameterDependencyRuleApiService.getRule(id).subscribe({
      next: (rule) => this.rule$.next(rule),
      error: (error) => console.error('Failed to load dependency rule details', error)
    });
  }

  /**
   * Handles the onEdit workflow.
   */
  onEdit(): void {
    const rule = this.rule$.value;
    if (!rule) {
      return;
    }
    this.router.navigate(['/rules', rule.id, 'edit']);
  }

  /**
   * Handles the onDelete workflow.
   */
  onDelete(): void {
    const rule = this.rule$.value;
    if (!rule) {
      return;
    }

    const confirmed = window.confirm(
      `Delete dependency rule ${rule.sourceDefinitionCode || '-'} -> ${rule.targetDefinitionCode || '-'}?`
    );
    if (!confirmed) {
      return;
    }

    this.parameterDependencyRuleApiService.deleteRule(rule.id).subscribe({
      next: () => this.router.navigate(['/rules']),
      error: (error) => console.error('Failed to delete dependency rule', error)
    });
  }
}

