import { NgFor } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigurationDefinition } from '../../../../../core/models/configuration-definition.model';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ParameterValueType } from '../../../../../core/models/parameter-value-type.model';
import { StepType } from '../../../../../core/models/step-type.model';
import { ConfigurationDefinitionApiService } from '../../../../../core/services/configuration-definition-api.service';

@Component({
  selector: 'app-parameter-definition-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor],
  templateUrl: './parameter-definition-form.component.html',
  styleUrl: './parameter-definition-form.component.scss'
})
/**
 * ParameterDefinitionFormComponent coordinates UI logic for this feature.
 */
export class ParameterDefinitionFormComponent implements OnInit {
  @Input() initialValue: ParameterDefinition | null = null;
  @Input() mode: 'create' | 'edit' = 'create';

  @Output() submitted = new EventEmitter<ParameterDefinition>();

  readonly valueTypes: ParameterValueType[] = ['STRING', 'NUMBER', 'BOOLEAN', 'ENUM', 'JSON'];
  readonly stepTypes: StepType[] = ['STEP', 'PRESTEP'];
  configurationDefinitions: ConfigurationDefinition[] = [];

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      alias: ['', Validators.required],
      unit: [''],
      description: [''],
      valueType: ['STRING' as ParameterValueType, Validators.required],
      requiredOnStep: [true],
      stepType: ['STEP' as StepType, Validators.required],
      defaultValueJson: [''],
      configurationDefinitionId: [null as number | null]
    });
  }

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    this.configurationDefinitionApiService.getDefinitions().subscribe({
      next: (definitions) => {
        this.configurationDefinitions = definitions;
      },
      error: (error) => {
        console.error('Failed to load configuration definitions', error);
        this.configurationDefinitions = [];
      }
    });

    if (this.initialValue) {
      this.form.patchValue({
        name: this.initialValue.name,
        alias: this.initialValue.alias,
        unit: this.initialValue.unit || '',
        description: this.initialValue.description || '',
        valueType: this.initialValue.valueType,
        requiredOnStep: this.initialValue.requiredOnStep,
        stepType: this.initialValue.stepType,
        defaultValueJson: this.initialValue.defaultValueJson || '',
        configurationDefinitionId: this.initialValue.configurationDefinitionId ?? null
      });
    }
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
      id: this.initialValue?.id,
      name: raw.name || '',
      alias: raw.alias || '',
      unit: raw.unit || null,
      description: raw.description || null,
      valueType: raw.valueType!,
      requiredOnStep: !!raw.requiredOnStep,
      stepType: raw.stepType!,
      defaultValueJson: raw.defaultValueJson || null,
      configurationDefinitionId: raw.configurationDefinitionId ?? null
    });
  }
}

