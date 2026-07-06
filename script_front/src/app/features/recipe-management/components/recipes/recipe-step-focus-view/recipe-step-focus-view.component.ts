import { NgFor, NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { Step } from '../../../../../core/models/step.model';
import { StepParameter } from '../../../../../core/models/step-parameter.model';
import { StepListComponent } from '../../steps/step-list/step-list.component';

@Component({
  selector: 'app-recipe-step-focus-view',
  standalone: true,
  imports: [NgIf, NgFor, StepListComponent],
  templateUrl: './recipe-step-focus-view.component.html',
  styleUrl: './recipe-step-focus-view.component.scss'
})
/**
 * RecipeStepFocusViewComponent coordinates UI logic for this feature.
 */
export class RecipeStepFocusViewComponent {
  @Input() steps: Step[] = [];
  @Input() selectedStepId?: number | null;
  @Input() selectedStep: Step | null = null;
  @Input() parameters: StepParameter[] = [];

  @Output() stepSelected = new EventEmitter<Step>();
  @Output() addParameterClicked = new EventEmitter<void>();
  @Output() deleteParameterClicked = new EventEmitter<StepParameter>();

  constructor(private router: Router) {}

  /**
   * Handles the goToDefinition workflow.
   */
  goToDefinition(parameter: StepParameter): void {
    if (!parameter.definitionId) return;
    this.router.navigate(['/parameter', parameter.definitionId]);
  }
}
