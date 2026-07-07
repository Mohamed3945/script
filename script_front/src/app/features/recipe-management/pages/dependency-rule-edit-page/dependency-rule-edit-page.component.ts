import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ParameterDependencyRule } from '../../../../core/models/parameter-dependency-rule.model';
import { ParameterDependencyRuleView } from '../../../../core/models/parameter-dependency-rule-view.model';
import { ParameterDependencyRuleApiService } from '../../../../core/services/parameter-dependency-rule-api.service';
import { DependencyRuleFormComponent } from '../../components/dependency-rules/dependency-rule-form/dependency-rule-form.component';

@Component({
  selector: 'app-dependency-rule-edit-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, DependencyRuleFormComponent],
  templateUrl: './dependency-rule-edit-page.component.html',
  styleUrl: './dependency-rule-edit-page.component.scss'
})
/**
 * DependencyRuleEditPageComponent coordinates UI logic for this feature.
 */
export class DependencyRuleEditPageComponent implements OnInit {
  ruleView$ = new BehaviorSubject<ParameterDependencyRuleView | null>(null);
  formValue$ = new BehaviorSubject<ParameterDependencyRule | null>(null);

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
      next: (rule) => {
        this.ruleView$.next(rule);
        this.formValue$.next({
          id: rule.id,
          sourceDefinitionId: rule.sourceDefinitionId,
          triggerOptionId: rule.triggerOptionId,
          requiredSourceActivationOptionId: rule.requiredSourceActivationOptionId ?? null,
          targetDefinitionId: rule.targetDefinitionId,
          effect: rule.effect,
          scope: rule.scope,
          priority: rule.priority
        });
      },
      error: (error) => console.error('Failed to load dependency rule', error)
    });
  }

  /**
   * Handles the onSubmit workflow.
   */
  onSubmit(payload: ParameterDependencyRule | ParameterDependencyRule[]): void {
    if (Array.isArray(payload)) {
      return;
    }

    const rule = payload;
    const current = this.ruleView$.value;
    if (!current) {
      return;
    }

    this.parameterDependencyRuleApiService.updateRule(current.id, rule).subscribe({
      next: () => this.router.navigate(['/recipes/rules', current.id]),
      error: (error) => console.error('Failed to update dependency rule', error)
    });
  }
}

