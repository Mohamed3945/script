import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { StepParameter } from '../../../../../core/models/step-parameter.model';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ParameterOption } from '../../../../../core/models/parameter-option.model';
import { ParameterDefinitionApiService } from '../../../../../core/services/parameter-definition-api.service';
import { ParameterOptionApiService } from '../../../../../core/services/parameter-option-api.service';

@Component({
  selector: 'app-step-parameter-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgFor],
  templateUrl: './step-parameter-form.component.html',
  styleUrl: './step-parameter-form.component.scss'
})
export class StepParameterFormComponent implements OnChanges {
  @Input() parentCandidates: StepParameter[] = [];
  @Output() submitted = new EventEmitter<StepParameter>();

  definitions: ParameterDefinition[] = [];
  availableOptions: ParameterOption[] = [];
  selectedDefinition?: ParameterDefinition;

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private parameterOptionApiService: ParameterOptionApiService
  ) {
    this.form = this.fb.group({
      definitionId: [null as number | null, Validators.required],
      parentStepParameterId: [null as number | null],
      orderIndex: [null as number | null],
      labelOverride: [''],
      valueJson: [''],
      selectedOptionId: [null as number | null],
      lockedByGolden: [false]
    });

    this.loadDefinitions();

    this.form.get('definitionId')?.valueChanges.subscribe((definitionId) => {
      this.onDefinitionChanged(definitionId ?? null);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {}

  get isEnumDefinition(): boolean {
    return this.selectedDefinition?.valueType === 'ENUM';
  }

  loadDefinitions(): void {
    this.parameterDefinitionApiService.getDefinitions().subscribe({
      next: (definitions) => {
        this.definitions = definitions;
      },
      error: (error) => {
        console.error('Failed to load parameter definitions', error);
        this.definitions = [];
      }
    });
  }

  onDefinitionChanged(definitionId: number | null): void {
    this.selectedDefinition = this.definitions.find(d => d.id === definitionId);

    this.form.patchValue({
      selectedOptionId: null,
      valueJson: ''
    }, { emitEvent: false });

    this.availableOptions = [];

    if (this.selectedDefinition?.valueType === 'ENUM' && definitionId) {
      this.parameterOptionApiService.getOptionsByDefinition(definitionId).subscribe({
        next: (options) => {
          this.availableOptions = options.sort((a, b) => a.orderIndex - b.orderIndex);
        },
        error: (error) => {
          console.error('Failed to load parameter options', error);
          this.availableOptions = [];
        }
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    const payload: StepParameter = {
      definitionId: raw.definitionId!,
      parentStepParameterId: raw.parentStepParameterId,
      orderIndex: raw.orderIndex,
      labelOverride: raw.labelOverride || null,
      valueJson: this.isEnumDefinition ? null : (raw.valueJson || null),
      selectedOptionId: this.isEnumDefinition ? raw.selectedOptionId : null,
      lockedByGolden: !!raw.lockedByGolden
    };

    this.submitted.emit(payload);

    this.form.reset({
      definitionId: null,
      parentStepParameterId: null,
      orderIndex: null,
      labelOverride: '',
      valueJson: '',
      selectedOptionId: null,
      lockedByGolden: false
    });

    this.availableOptions = [];
    this.selectedDefinition = undefined;
  }
}