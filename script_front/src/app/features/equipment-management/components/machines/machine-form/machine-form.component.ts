import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { NgFor } from '@angular/common';
import { Machine } from '../../../../../core/models/machine.model';
import { PlatformType } from '../../../../../core/models/platform-type.model';

@Component({
  selector: 'app-machine-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor],
  templateUrl: './machine-form.component.html',
  styleUrl: './machine-form.component.scss'
})
export class MachineFormComponent implements OnInit {
  @Input() initialValue: Machine | null = null;
  @Input() mode: 'create' | 'edit' = 'create';

  @Output() submitted = new EventEmitter<Machine>();

  readonly platformTypes: PlatformType[] = ['CENTURA', 'VANTAGE'];
  form: any;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      code: ['', Validators.required],
      name: ['', Validators.required],
      platformType: ['CENTURA' as PlatformType, Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.initialValue) {
      this.form.patchValue({
        code: this.initialValue.code,
        name: this.initialValue.name,
        platformType: this.initialValue.platformType
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
      name: raw.name || '',
      platformType: raw.platformType!
    });
  }
}
