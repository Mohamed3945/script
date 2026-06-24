import { NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Step } from '../../../../../core/models/step.model';
import { StepFormComponent } from '../step-form/step-form.component';

@Component({
  selector: 'app-step-modal',
  standalone: true,
  imports: [NgIf, StepFormComponent],
  templateUrl: './step-modal.component.html',
  styleUrl: './step-modal.component.scss'
})
export class StepModalComponent {
  @Input() visible = false;
  @Output() closed = new EventEmitter<void>();
  @Output() submitted = new EventEmitter<Step>();
}
