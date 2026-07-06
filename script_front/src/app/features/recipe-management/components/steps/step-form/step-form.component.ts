import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Step } from '../../../../../core/models/step.model';

@Component({
  selector: 'app-step-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './step-form.component.html',
  styleUrl: './step-form.component.scss'
})
/**
 * StepFormComponent coordinates UI logic for this feature.
 */
export class StepFormComponent {
  @Output() submitted = new EventEmitter<Step>();

  form: ReturnType<FormBuilder['group']>;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      stepKind: ['STEP' as Step['stepKind'], Validators.required],
      orderIndex: [null as number | null],
      name: ['', Validators.required]
    });
  }

  /**
   * Handles the onSubmit workflow.
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    this.submitted.emit({
      stepKind: raw.stepKind!,
      orderIndex: raw.orderIndex,
      name: raw.name || ''
    });

    this.form.reset({
      stepKind: 'STEP',
      orderIndex: null,
      name: ''
    });
  }
}
