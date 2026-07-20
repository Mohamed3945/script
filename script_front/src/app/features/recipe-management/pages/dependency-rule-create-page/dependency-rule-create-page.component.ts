import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { from, of } from 'rxjs';
import { concatMap, map, switchMap, toArray } from 'rxjs/operators';
import { ParameterDependencyRule } from '../../../../core/models/parameter-dependency-rule.model';
import { ParameterDependencyRuleApiService } from '../../../../core/services/parameter-dependency-rule-api.service';
import { DependencyRuleFormComponent } from '../../components/dependency-rules/dependency-rule-form/dependency-rule-form.component';

@Component({
  selector: 'app-dependency-rule-create-page',
  standalone: true,
  imports: [DependencyRuleFormComponent],
  templateUrl: './dependency-rule-create-page.component.html',
  styleUrl: './dependency-rule-create-page.component.scss'
})
/**
 * DependencyRuleCreatePageComponent coordinates UI logic for this feature.
 */
export class DependencyRuleCreatePageComponent {
  constructor(
    private parameterDependencyRuleApiService: ParameterDependencyRuleApiService,
    private router: Router
  ) {}

  /**
   * Handles the onSubmit workflow.
   */
  onSubmit(payload: ParameterDependencyRule | ParameterDependencyRule[]): void {
    const rules = Array.isArray(payload) ? payload : [payload];

    if (rules.length === 0) {
      return;
    }

    if (rules.length === 1) {
      this.parameterDependencyRuleApiService.createRule(rules[0]).subscribe({
        next: (created) => {
          if (created.id) {
            this.router.navigate(['/rules', created.id]);
            return;
          }
          this.router.navigate(['/rules']);
        },
        error: (error) => console.error('Failed to create dependency rule', error)
      });
      return;
    }

    this.parameterDependencyRuleApiService.getRules().pipe(
      map((existingRules) => {
        const existingKeys = new Set(
          existingRules.map((rule) =>
            this.toKey(
              rule.sourceDefinitionId,
              rule.triggerOptionId,
              rule.requiredSourceActivationOptionId ?? null,
              rule.targetDefinitionId
            )
          )
        );

        const uniquePayloadKeys = new Set<string>();

        return rules.filter((rule) => {
          const key = this.toKey(
            rule.sourceDefinitionId,
            rule.triggerOptionId,
            rule.requiredSourceActivationOptionId ?? null,
            rule.targetDefinitionId
          );

          if (existingKeys.has(key) || uniquePayloadKeys.has(key)) {
            return false;
          }

          uniquePayloadKeys.add(key);
          return true;
        });
      }),
      switchMap((filteredRules) => {
        if (filteredRules.length === 0) {
          return of([]);
        }

        return from(filteredRules).pipe(
          concatMap((rule) => this.parameterDependencyRuleApiService.createRule(rule)),
          toArray()
        );
      })
    ).subscribe({
      next: () => {
        this.router.navigate(['/rules']);
      },
      error: (error) => console.error('Failed to create dependency rules batch', error)
    });
  }

  private toKey(
    sourceDefinitionId: number,
    triggerOptionId: number,
    requiredSourceActivationOptionId: number | null,
    targetDefinitionId: number
  ): string {
    return `${sourceDefinitionId}-${triggerOptionId}-${requiredSourceActivationOptionId ?? 'null'}-${targetDefinitionId}`;
  }
}

