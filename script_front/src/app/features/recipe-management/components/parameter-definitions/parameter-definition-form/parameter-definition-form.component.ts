import { NgFor } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ParameterValueType } from '../../../../../core/models/parameter-value-type.model';

@Component({
  selector: 'app-parameter-definition-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor],
  templateUrl: './parameter-definition-form.component.html',
  styleUrl: './parameter-definition-form.component.scss'
})
export class ParameterDefinitionFormComponent implements OnInit {
  @Input() initialValue: ParameterDefinition | null = null;
  @Input() mode: 'create' | 'edit' = 'create';

  @Output() submitted = new EventEmitter<ParameterDefinition>();

  readonly valueTypes: ParameterValueType[] = ['STRING', 'NUMBER', 'BOOLEAN', 'ENUM', 'JSON'];

  form: ReturnType<FormBuilder['group']>;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      alias: ['', Validators.required],
      unit: [''],
      description: [''],
      valueType: ['STRING' as ParameterValueType, Validators.required],
      requiredOnStep: [true],
      defaultValueJson: ['']
    });
  }

  ngOnInit(): void {
    if (this.initialValue) {
      this.form.patchValue({
        name: this.initialValue.name,
        alias: this.initialValue.alias,
        unit: this.initialValue.unit || '',
        description: this.initialValue.description || '',
        valueType: this.initialValue.valueType,
        requiredOnStep: this.initialValue.requiredOnStep,
        defaultValueJson: this.initialValue.defaultValueJson || ''
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
      name: raw.name || '',
      alias: raw.alias || '',
      unit: raw.unit || null,
      description: raw.description || null,
      valueType: raw.valueType!,
      requiredOnStep: !!raw.requiredOnStep,
      defaultValueJson: raw.defaultValueJson || null
    });
  }
}

