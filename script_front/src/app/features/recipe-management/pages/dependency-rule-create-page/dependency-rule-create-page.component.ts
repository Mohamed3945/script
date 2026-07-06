import { Component } from '@angular/core';
import { Router } from '@angular/router';
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
  onSubmit(rule: ParameterDependencyRule): void {
    this.parameterDependencyRuleApiService.createRule(rule).subscribe({
      next: (created) => {
        if (created.id) {
          this.router.navigate(['/recipes/rules', created.id]);
          return;
        }
        this.router.navigate(['/recipes/rules']);
      },
      error: (error) => console.error('Failed to create dependency rule', error)
    });
  }
}

