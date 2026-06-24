import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Step } from '../../../../../core/models/step.model';
import { StepListComponent } from '../../steps/step-list/step-list.component';
import { StepFormComponent } from '../../steps/step-form/step-form.component';

@Component({
  selector: 'app-recipe-steps-panel',
  standalone: true,
  imports: [StepListComponent, StepFormComponent],
  templateUrl: './recipe-steps-panel.component.html',
  styleUrl: './recipe-steps-panel.component.scss'
})
export class RecipeStepsPanelComponent {
  @Input() steps: Step[] = [];
  @Input() selectedStepId?: number | null;

  @Output() stepSelected = new EventEmitter<Step>();
  @Output() stepCreated = new EventEmitter<Step>();
}