import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ConfigurationDefinition } from '../../../../../core/models/configuration-definition.model';
import { ConfigurationValueType } from '../../../../../core/models/configuration-value-type.model';

@Component({
  selector: 'app-definition-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './definition-form.component.html',
  styleUrl: './definition-form.component.scss'
})
export class DefinitionFormComponent implements OnInit {
  @Input() initialValue: ConfigurationDefinition | null = null;
  @Input() mode: 'create' | 'edit' = 'create';
  @Output() submitted = new EventEmitter<ConfigurationDefinition>();

  readonly valueTypes: ConfigurationValueType[] = ['BOOLEAN', 'NUMBER', 'ENUM', 'TEXT'];
  form: any;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      code: ['', Validators.required],
      name: ['', Validators.required],
      valueType: ['TEXT' as ConfigurationValueType, Validators.required],
      unit: [''],
      questionForForm: ['', Validators.required],
      questionGroup: [''],
      displayOrder: [0, Validators.required],
      active: [true]
    });
  }

  ngOnInit(): void {
    if (this.initialValue) {
      this.form.patchValue({
        code: this.initialValue.code,
        name: this.initialValue.name,
        valueType: this.initialValue.valueType,
        unit: this.initialValue.unit ?? '',
        questionForForm: this.initialValue.questionForForm,
        questionGroup: this.initialValue.questionGroup ?? '',
        displayOrder: this.initialValue.displayOrder,
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
      name: raw.name || '',
      valueType: (raw.valueType || 'TEXT') as ConfigurationValueType,
      unit: raw.unit || null,
      questionForForm: raw.questionForForm || '',
      questionGroup: raw.questionGroup || null,
      displayOrder: raw.displayOrder ?? 0,
      active: !!raw.active
    });
  }
}
