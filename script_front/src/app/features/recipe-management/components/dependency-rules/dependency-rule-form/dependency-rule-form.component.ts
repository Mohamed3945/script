import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ParameterDependencyRule } from '../../../../../core/models/parameter-dependency-rule.model';
import { RuleEffect } from '../../../../../core/models/rule-effect.model';
import { RuleScope } from '../../../../../core/models/rule-scope.model';
import { ParameterOption } from '../../../../../core/models/parameter-option.model';
import { ParameterDefinitionApiService } from '../../../../../core/services/parameter-definition-api.service';
import { ParameterOptionApiService } from '../../../../../core/services/parameter-option-api.service';
import { ParameterDependencyRuleApiService } from '../../../../../core/services/parameter-dependency-rule-api.service';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

interface SourceActivationContextOption {
  id: number;
  code: string;
  label: string;
  controllerDefinitionCode: string;
  controllerDefinitionName: string;
}

@Component({
  selector: 'app-dependency-rule-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor, NgIf],
  templateUrl: './dependency-rule-form.component.html',
  styleUrl: './dependency-rule-form.component.scss'
})
/**
 * DependencyRuleFormComponent coordinates UI logic for this feature.
 */
export class DependencyRuleFormComponent implements OnInit {
  @Input() initialValue: ParameterDependencyRule | null = null;
  @Input() mode: 'create' | 'edit' = 'create';

  @Output() submitted = new EventEmitter<ParameterDependencyRule | ParameterDependencyRule[]>();

  definitions: ParameterDefinition[] = [];
  sourceDefinitions: ParameterDefinition[] = [];
  targetDefinitions: ParameterDefinition[] = [];
  sourceOptions: ParameterOption[] = [];
  sourceActivationContextOptions: SourceActivationContextOption[] = [];
  hasControllerContext = false;
  selectedTriggerOptionIds: number[] = [];
  selectedTargetDefinitionIds: number[] = [];
  createSelectionError = false;

  readonly effects: RuleEffect[] = ['ENABLE', 'DISABLE'];
  readonly scopes: RuleScope[] = ['STEP', 'RECIPE'];

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private parameterOptionApiService: ParameterOptionApiService,
    private parameterDependencyRuleApiService: ParameterDependencyRuleApiService
  ) {
    this.form = this.fb.group({
      sourceDefinitionId: [null as number | null, Validators.required],
      triggerOptionId: [null as number | null],
      requiredSourceActivationOptionId: [null as number | null],
      targetDefinitionId: [null as number | null],
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
        this.sourceActivationContextOptions = [];
        this.hasControllerContext = false;
        this.selectedTriggerOptionIds = [];
        this.selectedTargetDefinitionIds = [];
        this.createSelectionError = false;
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
          const requiredStillValid = this.sourceActivationContextOptions.some((o) => o.id === currentRequired);

          if (this.mode === 'create') {
            this.selectedTriggerOptionIds = this.selectedTriggerOptionIds.filter((id) =>
              this.sourceOptions.some((option) => option.id === id)
            );
          }

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

      this.loadSourceActivationContextOptions(definitionId, currentRequired);
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

      if (this.mode === 'edit') {
        this.selectedTriggerOptionIds = [this.initialValue.triggerOptionId];
        this.selectedTargetDefinitionIds = [this.initialValue.targetDefinitionId];
      }
    }
  }

  /**
   * Handles the loadDefinitions workflow.
   */
  loadDefinitions(): void {
    this.parameterDefinitionApiService.getDefinitions().subscribe({
      next: (definitions) => {
        this.definitions = definitions;
        this.sourceDefinitions = definitions.filter((definition) => definition.valueType === 'ENUM');
        this.targetDefinitions = definitions;
      },
      error: (error) => {
        console.error('Failed to load parameter definitions', error);
      }
    });
  }

  private loadSourceActivationContextOptions(sourceDefinitionId: number, currentRequired: number | null): void {
    this.parameterDependencyRuleApiService.getRules().subscribe({
      next: (rules) => {
        const controllerById = new Map<number, { code: string; name: string }>();

        rules
          .filter((rule) => rule.targetDefinitionId === sourceDefinitionId)
          .forEach((rule) => {
            controllerById.set(rule.sourceDefinitionId, {
              code: rule.sourceDefinitionCode,
              name: rule.sourceDefinitionName
            });
          });

        const controllerDefinitionIds = Array.from(controllerById.keys());
        this.hasControllerContext = controllerDefinitionIds.length > 0;

        if (controllerDefinitionIds.length === 0) {
          this.sourceActivationContextOptions = [];
          this.form.patchValue(
            {
              requiredSourceActivationOptionId: null
            },
            { emitEvent: false }
          );
          return;
        }

        const optionRequests = controllerDefinitionIds.map((definitionId) =>
          this.parameterOptionApiService.getOptionsByDefinition(definitionId).pipe(
            map((options) => ({
              definitionId,
              options
            }))
          )
        );

        forkJoin(optionRequests).subscribe({
          next: (controllerOptions) => {
            this.sourceActivationContextOptions = controllerOptions
              .flatMap(({ definitionId, options }) => {
                const controller = controllerById.get(definitionId);

                return [...options]
                  .sort((a, b) => a.orderIndex - b.orderIndex)
                  .filter((option): option is ParameterOption & { id: number } => option.id !== undefined)
                  .map((option) => ({
                    id: option.id,
                    code: option.code || '-',
                    label: option.label,
                    controllerDefinitionCode: controller?.code || '-',
                    controllerDefinitionName: controller?.name || '-'
                  }));
              })
              .sort(
                (a, b) =>
                  a.controllerDefinitionCode.localeCompare(b.controllerDefinitionCode)
                  || a.label.localeCompare(b.label)
              );

            const requiredStillValid = this.sourceActivationContextOptions.some((option) => option.id === currentRequired);

            this.form.patchValue(
              {
                requiredSourceActivationOptionId: requiredStillValid ? currentRequired : null
              },
              { emitEvent: false }
            );
          },
          error: (error) => {
            console.error('Failed to load source activation context options', error);
            this.sourceActivationContextOptions = [];
            this.hasControllerContext = false;
            this.form.patchValue(
              {
                requiredSourceActivationOptionId: null
              },
              { emitEvent: false }
            );
          }
        });
      },
      error: (error) => {
        console.error('Failed to load dependency rules for source activation context', error);
        this.sourceActivationContextOptions = [];
        this.hasControllerContext = false;
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

    if (this.mode === 'create') {
      const sourceDefinitionId = raw.sourceDefinitionId;
      const validTargetDefinitionIds = this.selectedTargetDefinitionIds.filter((targetId) => targetId !== sourceDefinitionId);

      if (!sourceDefinitionId || this.selectedTriggerOptionIds.length === 0 || validTargetDefinitionIds.length === 0) {
        this.createSelectionError = true;
        this.form.markAllAsTouched();
        return;
      }

      this.createSelectionError = false;

      const payloads: ParameterDependencyRule[] = this.selectedTriggerOptionIds.flatMap((triggerOptionId) =>
        validTargetDefinitionIds.map((targetDefinitionId) => ({
          sourceDefinitionId,
          triggerOptionId,
          requiredSourceActivationOptionId: raw.requiredSourceActivationOptionId ?? null,
          targetDefinitionId,
          effect: raw.effect!,
          scope: raw.scope!,
          priority: raw.priority ?? 0
        }))
      );

      this.submitted.emit(payloads);
      return;
    }

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

  toggleTriggerOption(optionId: number | undefined, checked: boolean): void {
    if (optionId === undefined) {
      return;
    }

    if (checked) {
      if (!this.selectedTriggerOptionIds.includes(optionId)) {
        this.selectedTriggerOptionIds = [...this.selectedTriggerOptionIds, optionId];
      }
    } else {
      this.selectedTriggerOptionIds = this.selectedTriggerOptionIds.filter((id) => id !== optionId);
    }
  }

  toggleTargetDefinition(definitionId: number | undefined, checked: boolean): void {
    if (definitionId === undefined) {
      return;
    }

    const sourceDefinitionId = this.form.getRawValue().sourceDefinitionId;
    if (definitionId === sourceDefinitionId) {
      return;
    }

    if (checked) {
      if (!this.selectedTargetDefinitionIds.includes(definitionId)) {
        this.selectedTargetDefinitionIds = [...this.selectedTargetDefinitionIds, definitionId];
      }
    } else {
      this.selectedTargetDefinitionIds = this.selectedTargetDefinitionIds.filter((id) => id !== definitionId);
    }
  }

  isTriggerSelected(optionId: number | undefined): boolean {
    if (optionId === undefined) {
      return false;
    }
    return this.selectedTriggerOptionIds.includes(optionId);
  }

  isTargetSelected(definitionId: number | undefined): boolean {
    if (definitionId === undefined) {
      return false;
    }
    return this.selectedTargetDefinitionIds.includes(definitionId);
  }
}

