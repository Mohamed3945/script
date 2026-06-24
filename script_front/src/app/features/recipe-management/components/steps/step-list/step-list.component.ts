import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Step } from '../../../../../core/models/step.model';

@Component({
  selector: 'app-step-list',
  standalone: true,
  imports: [NgFor, NgIf],
  templateUrl: './step-list.component.html',
  styleUrl: './step-list.component.scss'
})
export class StepListComponent {
  @Input() steps: Step[] = [];
  @Input() selectedStepId?: number | null;

  @Output() stepSelected = new EventEmitter<Step>();
}