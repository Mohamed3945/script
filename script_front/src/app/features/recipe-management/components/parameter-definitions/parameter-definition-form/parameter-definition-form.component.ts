import { NgFor } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigurationDefinition } from '../../../../../core/models/configuration-definition.model';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ParameterGroup } from '../../../../../core/models/parameter-group.model';
import { ParameterValueType } from '../../../../../core/models/parameter-value-type.model';
import { ParameterScope } from '../../../../../core/models/parameter-scope.model';
import { ConfigurationDefinitionApiService } from '../../../../../core/services/configuration-definition-api.service';
import { ParameterGroupApiService } from '../../../../../core/services/parameter-group-api.service';

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
  readonly stepTypes: ParameterScope[] = ['STEP', 'PRESTEP', 'ENDPOINT'];

  configurationDefinitions: ConfigurationDefinition[] = [];
  parameterGroups: ParameterGroup[] = [];

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService,
    private parameterGroupApiService: ParameterGroupApiService
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      alias: ['', Validators.required],
      unit: [''],
      description: [''],
      valueType: ['STRING' as ParameterValueType, Validators.required],
      requiredOnStep: [true],
      stepType: ['STEP' as ParameterScope, Validators.required],
      defaultValueJson: [''],
      parameterGroupId: [null as number | null],
      configurationDefinitionId: [null as number | null]
    });
  }

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

    this.form.get('stepType')?.valueChanges.subscribe((stepType) => {
      this.loadGroups(stepType ?? 'STEP');
    });

    const initialStepType = this.initialValue?.stepType ?? 'STEP';
    this.loadGroups(initialStepType);

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
        parameterGroupId: this.initialValue.parameterGroupId ?? null,
        configurationDefinitionId: this.initialValue.configurationDefinitionId ?? null
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const selectedGroup = this.parameterGroups.find(group => group.id === (raw.parameterGroupId ?? null));

    this.submitted.emit({
      id: this.initialValue?.id,
      code: this.initialValue?.code,
      name: raw.name || '',
      alias: raw.alias || '',
      unit: raw.unit || null,
      description: raw.description || null,
      valueType: raw.valueType!,
      requiredOnStep: !!raw.requiredOnStep,
      stepType: raw.stepType!,
      defaultValueJson: raw.defaultValueJson || null,
      parameterGroupId: raw.parameterGroupId ?? null,
      parameterGroupName: selectedGroup?.name ?? null,
      parameterGroupOrder: selectedGroup?.orderIndex ?? null,
      orderIndexInGroup: this.initialValue?.orderIndexInGroup ?? null,
      configurationDefinitionId: raw.configurationDefinitionId ?? null
    });
  }

  private loadGroups(stepType: ParameterScope): void {
    this.parameterGroupApiService.getGroups(stepType).subscribe({
      next: (groups) => {
        this.parameterGroups = groups;
        const selectedGroupId = this.form.get('parameterGroupId')?.value ?? null;
        if (selectedGroupId != null && !groups.some(group => group.id === selectedGroupId)) {
          this.form.patchValue({ parameterGroupId: null }, { emitEvent: false });
        }
      },
      error: (error) => {
        console.error('Failed to load parameter groups', error);
        this.parameterGroups = [];
      }
    });
  }
}