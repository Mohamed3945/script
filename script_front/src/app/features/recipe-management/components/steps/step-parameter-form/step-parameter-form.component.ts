import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { StepParameter } from '../../../../../core/models/step-parameter.model';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ParameterOption } from '../../../../../core/models/parameter-option.model';
import { StepKind } from '../../../../../core/models/step-kind.model';
import { ParameterScope } from '../../../../../core/models/parameter-scope.model';
import { ParameterDefinitionApiService } from '../../../../../core/services/parameter-definition-api.service';
import { ParameterOptionApiService } from '../../../../../core/services/parameter-option-api.service';

@Component({
  selector: 'app-step-parameter-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgFor],
  templateUrl: './step-parameter-form.component.html',
  styleUrl: './step-parameter-form.component.scss'
})
/**
 * StepParameterFormComponent coordinates UI logic for this feature.
 */
export class StepParameterFormComponent implements OnChanges {
  @Input() visible = false;
  @Input() stepKind: StepKind | null = null;
  @Input() parentCandidates: StepParameter[] = [];
  @Input() excludedDefinitionIds: number[] = [];
  @Output() submitted = new EventEmitter<StepParameter>();

  allDefinitions: ParameterDefinition[] = [];
  definitions: ParameterDefinition[] = [];
  availableOptions: ParameterOption[] = [];
  selectedDefinition?: ParameterDefinition;
  loadingDefinitions = false;
  definitionLoadError = false;
  private definitionsRequestSeq = 0;

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

    this.form.get('definitionId')?.valueChanges.subscribe((definitionId) => {
      this.onDefinitionChanged(definitionId ?? null);
    });
  }

  /**
   * Handles the ngOnChanges workflow.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (this.visible && (changes['visible']?.currentValue === true || changes['stepKind'])) {
      this.loadDefinitions();
      return;
    }

    if (this.visible && changes['excludedDefinitionIds']) {
      this.applyDefinitionFilter();
    }
  }

  get isEnumDefinition(): boolean {
    return this.selectedDefinition?.valueType === 'ENUM';
  }

  /**
   * Handles the loadDefinitions workflow.
   */
  loadDefinitions(): void {
    if (!this.stepKind) {
      this.allDefinitions = [];
      this.definitions = [];
      this.loadingDefinitions = false;
      this.definitionLoadError = false;
      return;
    }

    const requestSeq = ++this.definitionsRequestSeq;
    this.loadingDefinitions = true;
    this.definitionLoadError = false;

    const stepType: ParameterScope = this.stepKind === 'PRESTEP' ? 'PRESTEP' : 'STEP';

    this.parameterDefinitionApiService.getDefinitions(stepType).subscribe({
      next: (definitions) => {
        if (requestSeq !== this.definitionsRequestSeq) {
          return;
        }

        this.allDefinitions = [...definitions].sort((a, b) => a.name.localeCompare(b.name));
        this.applyDefinitionFilter();

        const selectedDefinitionId = this.form.get('definitionId')?.value ?? null;
        if (!this.definitions.some(d => d.id === selectedDefinitionId)) {
          this.form.patchValue({ definitionId: null }, { emitEvent: true });
        }
        this.loadingDefinitions = false;
      },
      error: (error) => {
        if (requestSeq !== this.definitionsRequestSeq) {
          return;
        }

        console.error('Failed to load parameter definitions', error);
        this.allDefinitions = [];
        this.definitions = [];
        this.loadingDefinitions = false;
        this.definitionLoadError = true;
      }
    });
  }

  private applyDefinitionFilter(): void {
    const excluded = new Set(this.excludedDefinitionIds ?? []);
    this.definitions = this.allDefinitions.filter((definition) => {
      if (definition.id == null) {
        return true;
      }

      return !excluded.has(definition.id);
    });

    const selectedDefinitionId = this.form.get('definitionId')?.value ?? null;
    if (!this.definitions.some((definition) => definition.id === selectedDefinitionId)) {
      this.selectedDefinition = undefined;
      this.availableOptions = [];
      if (selectedDefinitionId != null) {
        this.form.patchValue(
          {
            definitionId: null,
            selectedOptionId: null,
            valueJson: ''
          },
          { emitEvent: false }
        );
      }
    }
  }

  /**
   * Handles the onDefinitionChanged workflow.
   */
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

  /**
   * Handles the onSubmit workflow.
   */
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
