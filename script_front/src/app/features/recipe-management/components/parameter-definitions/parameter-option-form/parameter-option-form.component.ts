import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ParameterOption } from '../../../../../core/models/parameter-option.model';

@Component({
  selector: 'app-parameter-option-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './parameter-option-form.component.html',
  styleUrl: './parameter-option-form.component.scss'
})
export class ParameterOptionFormComponent implements OnChanges {
  @Input() definitionId!: number;
  @Input() initialValue: ParameterOption | null = null;

  @Output() submitted = new EventEmitter<ParameterOption>();
  @Output() cancelled = new EventEmitter<void>();

  form: ReturnType<FormBuilder['group']>;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      label: ['', Validators.required],
      orderIndex: [0, Validators.required]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.initialValue) {
      this.form.patchValue({
        label: this.initialValue.label,
        orderIndex: this.initialValue.orderIndex
      });
    } else {
      this.form.reset({
        label: '',
        orderIndex: 0
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid || !this.definitionId) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    this.submitted.emit({
      id: this.initialValue?.id,
      definitionId: this.definitionId,
      label: raw.label || '',
      orderIndex: raw.orderIndex ?? 0
    });

    if (!this.initialValue) {
      this.form.reset({
        label: '',
        orderIndex: 0
      });
    }
  }
}

