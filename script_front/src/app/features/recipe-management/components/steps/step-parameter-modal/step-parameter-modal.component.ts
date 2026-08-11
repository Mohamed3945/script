import { NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { StepParameter } from '../../../../../core/models/step-parameter.model';
import { StepKind } from '../../../../../core/models/step-kind.model';
import { StepParameterFormComponent } from '../step-parameter-form/step-parameter-form.component';

@Component({
  selector: 'app-step-parameter-modal',
  standalone: true,
  imports: [NgIf, StepParameterFormComponent],
  templateUrl: './step-parameter-modal.component.html',
  styleUrl: './step-parameter-modal.component.scss'
})
/**
 * StepParameterModalComponent coordinates UI logic for this feature.
 */
export class StepParameterModalComponent {
  @Input() visible = false;
  @Input() stepKind: StepKind | null = null;
  @Input() parentCandidates: StepParameter[] = [];
  @Input() excludedDefinitionIds: number[] = [];

  @Output() closed = new EventEmitter<void>();
  @Output() submitted = new EventEmitter<StepParameter>();
}
