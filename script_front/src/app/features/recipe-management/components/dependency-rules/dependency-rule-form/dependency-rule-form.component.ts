import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgFor } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ParameterDependencyRule } from '../../../../../core/models/parameter-dependency-rule.model';
import { RuleEffect } from '../../../../../core/models/rule-effect.model';
import { RuleScope } from '../../../../../core/models/rule-scope.model';
import { ParameterOption } from '../../../../../core/models/parameter-option.model';
import { ParameterDefinitionApiService } from '../../../../../core/services/parameter-definition-api.service';
import { ParameterOptionApiService } from '../../../../../core/services/parameter-option-api.service';

@Component({
  selector: 'app-dependency-rule-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor],
  templateUrl: './dependency-rule-form.component.html',
  styleUrl: './dependency-rule-form.component.scss'
})
/**
 * DependencyRuleFormComponent coordinates UI logic for this feature.
 */
export class DependencyRuleFormComponent implements OnInit {
  @Input() initialValue: ParameterDependencyRule | null = null;
  @Input() mode: 'create' | 'edit' = 'create';

  @Output() submitted = new EventEmitter<ParameterDependencyRule>();

  definitions: ParameterDefinition[] = [];
  sourceOptions: ParameterOption[] = [];

  readonly effects: RuleEffect[] = ['ENABLE', 'DISABLE'];
  readonly scopes: RuleScope[] = ['STEP', 'RECIPE'];

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private parameterOptionApiService: ParameterOptionApiService
  ) {
    this.form = this.fb.group({
      sourceDefinitionId: [null as number | null, Validators.required],
      triggerOptionId: [null as number | null, Validators.required],
      requiredSourceActivationOptionId: [null as number | null],
      targetDefinitionId: [null as number | null, Validators.required],
      effect: ['ENABLE' as RuleEffect, Validators.required],
      scope: ['STEP' as RuleScope, Validators.required],
      priority: [0, Validators.required]
    });
  }

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    this.loadDefinitions();

    this.form.get('sourceDefinitionId')?.valueChanges.subscribe((definitionId) => {
      const currentTrigger = this.form.getRawValue().triggerOptionId;
      const currentRequired = this.form.getRawValue().requiredSourceActivationOptionId;

      if (!definitionId) {
        this.sourceOptions = [];
        this.form.patchValue(
          {
            triggerOptionId: null,
            requiredSourceActivationOptionId: null
          },
          { emitEvent: false }
        );
        return;
      }

      this.parameterOptionApiService.getOptionsByDefinition(definitionId).subscribe({
        next: (options) => {
          this.sourceOptions = [...options].sort((a, b) => a.orderIndex - b.orderIndex);

          const triggerStillValid = this.sourceOptions.some((o) => o.id === currentTrigger);
          const requiredStillValid = this.sourceOptions.some((o) => o.id === currentRequired);

          this.form.patchValue(
            {
              triggerOptionId: triggerStillValid ? currentTrigger : null,
              requiredSourceActivationOptionId: requiredStillValid ? currentRequired : null
            },
            { emitEvent: false }
          );
        },
        error: (error) => {
          console.error('Failed to load source options', error);
          this.sourceOptions = [];
        }
      });
    });

    if (this.initialValue) {
      this.form.patchValue({
        sourceDefinitionId: this.initialValue.sourceDefinitionId,
        triggerOptionId: this.initialValue.triggerOptionId,
        requiredSourceActivationOptionId: this.initialValue.requiredSourceActivationOptionId ?? null,
        targetDefinitionId: this.initialValue.targetDefinitionId,
        effect: this.initialValue.effect,
        scope: this.initialValue.scope,
        priority: this.initialValue.priority
      });
    }
  }

  /**
   * Handles the loadDefinitions workflow.
   */
  loadDefinitions(): void {
    this.parameterDefinitionApiService.getDefinitions().subscribe({
      next: (definitions) => {
        this.definitions = definitions;
      },
      error: (error) => {
        console.error('Failed to load parameter definitions', error);
      }
    });
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
      sourceDefinitionId: raw.sourceDefinitionId!,
      triggerOptionId: raw.triggerOptionId!,
      requiredSourceActivationOptionId: raw.requiredSourceActivationOptionId ?? null,
      targetDefinitionId: raw.targetDefinitionId!,
      effect: raw.effect!,
      scope: raw.scope!,
      priority: raw.priority ?? 0
    });
  }
}

