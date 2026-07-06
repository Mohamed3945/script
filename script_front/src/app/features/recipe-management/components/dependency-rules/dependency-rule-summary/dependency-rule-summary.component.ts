import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';
import { ParameterDependencyRuleView } from '../../../../../core/models/parameter-dependency-rule-view.model';

@Component({
  selector: 'app-dependency-rule-summary',
  standalone: true,
  imports: [NgIf],
  templateUrl: './dependency-rule-summary.component.html',
  styleUrl: './dependency-rule-summary.component.scss'
})
/**
 * DependencyRuleSummaryComponent coordinates UI logic for this feature.
 */
export class DependencyRuleSummaryComponent {
  @Input() rule!: ParameterDependencyRuleView;
}

