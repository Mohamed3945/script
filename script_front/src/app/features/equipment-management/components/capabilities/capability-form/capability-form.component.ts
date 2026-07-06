import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ChamberCapability } from '../../../../../core/models/chamber-capability.model';
import { CapabilityCategory } from '../../../../../core/models/capability-category.model';

@Component({
  selector: 'app-capability-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './capability-form.component.html',
  styleUrl: './capability-form.component.scss'
})
export class CapabilityFormComponent implements OnInit {
  @Input() initialValue: ChamberCapability | null = null;
  @Input() mode: 'create' | 'edit' = 'create';
  @Output() submitted = new EventEmitter<ChamberCapability>();

  readonly categories: CapabilityCategory[] = ['TECHNO', 'MODE', 'TEMPERATURE', 'PRESSURE', 'GAS_FLOW', 'OTHER'];
  form: any;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      code: ['', Validators.required],
      label: ['', Validators.required],
      category: ['OTHER' as CapabilityCategory, Validators.required],
      active: [true]
    });
  }

  ngOnInit(): void {
    if (this.initialValue) {
      this.form.patchValue({
        code: this.initialValue.code,
        label: this.initialValue.label,
        category: this.initialValue.category,
        active: this.initialValue.active
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    this.submitted.emit({
      id: this.initialValue?.id,
      code: raw.code || '',
      label: raw.label || '',
      category: (raw.category || 'OTHER') as CapabilityCategory,
      active: !!raw.active
    });
  }
}
