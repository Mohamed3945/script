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
}

