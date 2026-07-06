import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Chamber } from '../../../../../core/models/chamber.model';

@Component({
  selector: 'app-chamber-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './chamber-form.component.html',
  styleUrl: './chamber-form.component.scss'
})
export class ChamberFormComponent implements OnInit {
  @Input() initialValue: Chamber | null = null;
  @Input() mode: 'create' | 'edit' = 'create';

  @Output() submitted = new EventEmitter<Chamber>();
  form: any;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      code: ['', Validators.required],
      name: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.initialValue) {
      this.form.patchValue({
        code: this.initialValue.code,
        name: this.initialValue.name
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
      machineId: this.initialValue?.machineId,
      machineCode: this.initialValue?.machineCode,
      machineName: this.initialValue?.machineName,
      code: raw.code || '',
      name: raw.name || ''
    });
  }
}
