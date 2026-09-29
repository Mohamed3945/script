import { AsyncPipe, NgClass, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { MachineChoice } from '../../../../core/models/machine-choice.model';
import { DecisionFinalizeRequest } from '../../../../core/models/decision-finalize-request.model';
import { RecipeMatrixCell } from '../../../../core/models/recipe-matrix-cell.model';
import { RecipeRequirements } from '../../../../core/models/recipe-requirements.model';
import { RecipeCompatibilityResult } from '../../../../core/models/recipe-compatibility-result.model';
import { RecipeRequirementsApiService } from '../../../../core/services/recipe-requirements-api.service';
import { RecipeCompatibilityApiService } from '../../../../core/services/recipe-compatibility-api.service';
import { DecisionWizardService } from '../../../../core/services/decision-wizard.service';
import { DecisionFinalizeApiService } from '../../../../core/services/decision-finalize-api.service';
import { buildRecipeDetailRouteByKind } from '../../../../core/utils/recipe-route.util';
import { ResultSummaryComponent } from '../../components/result-summary/result-summary.component';
import { ResultFinalActionsComponent } from '../../components/result-final-actions/result-final-actions.component';

interface LegendMatrixRow {
  parameterName: string;
  cells: RecipeMatrixCell[];
}

interface ColorLegendItem {
  cssClass: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-decision-result-page',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    NgClass,
    AsyncPipe,
    ResultSummaryComponent,
    ResultFinalActionsComponent
  ],
  templateUrl: './decision-result-page.component.html',
  styleUrl: './decision-result-page.component.scss'
})
export class DecisionResultPageComponent {
  readonly legendColumns = ['STEP_01', 'STEP_02', 'STEP_03', 'STEP_04', 'STEP_05'];
  readonly legendRows: LegendMatrixRow[] = this.buildLegendRows();
  readonly colorLegendItems: ColorLegendItem[] = [
    {
      cssClass: 'golden-active',
      title: 'Golden active',
      description: 'Value inherited from golden baseline and currently active.'
    },
    {
      cssClass: 'golden-inactive',
      title: 'Golden inactive',
      description: 'Golden value exists but the rule context disables this cell.'
    },
    {
      cssClass: 'computed',
      title: 'Computed',
      description: 'Value is calculated by runtime logic, not manually edited.'
    },
    {
      cssClass: 'editable-default',
      title: 'Editable default',
      description: 'Editable value that keeps the current default state.'
    },
    {
      cssClass: 'user-modified',
      title: 'User modified',
      description: 'Editable value changed by the user in derived customization.'
    },
    {
      cssClass: 'computer-modified-after-computation',
      title: 'Computer modified after computation',
      description: 'Editable value changed by the system in derived customization.'
    },
    {
      cssClass: 'readonly-neutral',
      title: 'Read-only neutral',
      description: 'Displayed for context only; no direct edit allowed.'
    }
  ];

  private readonly wizardService = inject(DecisionWizardService);
  private readonly decisionFinalizeApiService = inject(DecisionFinalizeApiService);
  private readonly recipeRequirementsApiService = inject(RecipeRequirementsApiService);
  private readonly recipeCompatibilityApiService = inject(RecipeCompatibilityApiService);
  private readonly router = inject(Router);

  result$ = this.wizardService.result$;
  selections$ = this.wizardService.selections$;

  loading$ = new BehaviorSubject<boolean>(false);
  requirements$ = new BehaviorSubject<RecipeRequirements | null>(null);
  compatibility$ = new BehaviorSubject<RecipeCompatibilityResult | null>(null);

  goldenValidated = false;
  machineChoices: MachineChoice[] = [];
  selectedMachineId: number | null = null;

  constructor() {
    const result = this.wizardService.resultSnapshot ?? null;
    if (result?.goldenRecipeId) {
      this.loadResultContext(result.goldenRecipeId, result.machineId ?? null);
    }
  }

  get canSubmit(): boolean {
    return this.goldenValidated && Boolean(this.selectedMachineId);
  }

  toggleGoldenValidated(): void {
    this.goldenValidated = !this.goldenValidated;
  }

  onMachineSelected(machineId: number): void {
    this.selectedMachineId = machineId;
    this.machineChoices = this.machineChoices.map((machine) => ({
      ...machine,
      selected: machine.id === machineId
    }));
  }

  restart(): void {
    this.wizardService.reset();
    this.router.navigate(['/wizard']);
  }

  saveAndCustomize(): void {
    const result = this.wizardService.resultSnapshot ?? null;

    if (!result?.id || !result.goldenRecipeId || !this.selectedMachineId || !this.goldenValidated) {
      return;
    }

    const payload: DecisionFinalizeRequest = {
      resultProfileId: result.id,
      validatedGoldenRecipeId: result.goldenRecipeId,
      selectedMachineId: this.selectedMachineId,
      creatorId: 1,
      answers: this.wizardService.buildExecutionAnswers()
    };

    this.loading$.next(true);

    this.decisionFinalizeApiService.finalizeDecision(payload).subscribe({
      next: (response) => {
        this.loading$.next(false);
        this.router.navigate(buildRecipeDetailRouteByKind(response.derivedRecipeId, 'DERIVED'), {
          state: { compatibleMachines: this.machineChoices }
        });
      },
      error: (error) => {
        console.error('Failed to finalize decision and create derived recipe', error);
        this.loading$.next(false);
      }
    });
  }

  getCellStateClass(cell: RecipeMatrixCell | null): string {
    if (!cell) {
      return 'readonly-neutral';
    }
    if (cell.lockedByGolden) {
      return cell.activationState === 'DISABLED' ? 'golden-inactive' : 'golden-active';
    }
    if (cell.computedFromModified) {
      return 'computer-modified-after-computation';
    }
    if (cell.computed) {
      return 'computed';
    }
    if (cell.editable && cell.userModified) {
      return 'user-modified';
    }
    if (cell.editable) {
      return 'editable-default';
    }
    return 'readonly-neutral';
  }

  private loadResultContext(goldenRecipeId: number, preferredMachineId: number | null): void {
    this.recipeRequirementsApiService.getRequirements(goldenRecipeId).subscribe({
      next: (requirements) => this.requirements$.next(requirements),
      error: (error) => console.error('Failed to load golden requirements', error)
    });

    this.recipeCompatibilityApiService
      .findCompatibleMachines({
        recipeId: goldenRecipeId,
        configurationConstraints: []
      })
      .subscribe({
        next: (compatibility) => {
          this.compatibility$.next(compatibility);
          const choices = compatibility.machines.map((machine) => ({
            id: machine.machineId,
            code: machine.machineCode,
            label: `${machine.machineName} (${machine.compatibleChambers.length} chamber${machine.compatibleChambers.length > 1 ? 's' : ''})`,
            selected: false
          }));
          this.machineChoices = choices;

          const defaultId = preferredMachineId && choices.some((m) => m.id === preferredMachineId)
            ? preferredMachineId
            : (choices[0]?.id ?? null);

          this.selectedMachineId = defaultId;
          this.machineChoices = choices.map((machine) => ({
            ...machine,
            selected: machine.id === defaultId
          }));
        },
        error: (error) => {
          console.error('Failed to load compatible machines from golden requirements', error);
          this.machineChoices = preferredMachineId
            ? [
                {
                  id: preferredMachineId,
                  code: `MACHINE_${preferredMachineId}`,
                  label: `Compatible machine #${preferredMachineId}`,
                  selected: true
                }
              ]
            : [];
          this.selectedMachineId = preferredMachineId;
        }
      });
  }

  private buildLegendRows(): LegendMatrixRow[] {
    return [
      {
        parameterName: 'Param 1',
        cells: [
          this.createLegendCell(1, 'ENUM', 'CH_A', 'golden-active', { selectedOptionLabel: 'CH_A' }),
          this.createLegendCell(2, 'ENUM', 'CH_B', 'golden-inactive', { selectedOptionLabel: 'CH_B', activationState: 'DISABLED' }),
          this.createLegendCell(3, 'ENUM', 'CH_C', 'editable-default', { selectedOptionLabel: 'CH_C', editable: true }),
          this.createLegendCell(4, 'ENUM', 'CH_D', 'user-modified', { selectedOptionLabel: 'CH_D', editable: true, userModified: true }),
          this.createLegendCell(5, 'ENUM', 'CH_E', 'computed', { selectedOptionLabel: 'CH_E', computed: true })
        ]
      },
      {
        parameterName: 'Param 2',
        cells: [
          this.createLegendCell(1, 'NUMBER', '120', 'golden-active', { valueJson: '120' }),
          this.createLegendCell(2, 'NUMBER', '135', 'editable-default', { valueJson: '135', editable: true }),
          this.createLegendCell(3, 'NUMBER', '150', 'user-modified', { valueJson: '150', editable: true, userModified: true }),
          this.createLegendCell(4, 'NUMBER', '145', 'computed', { valueJson: '145', computed: true }),
          this.createLegendCell(5, 'NUMBER', '—', 'golden-inactive', { activationState: 'DISABLED' })
        ]
      },
      {
        parameterName: 'Param 3',
        cells: [
          this.createLegendCell(1, 'ENUM', 'CONST_VOLTAGE', 'golden-active', { selectedOptionLabel: 'CONST_VOLTAGE' }),
          this.createLegendCell(2, 'ENUM', 'CONST_CURRENT', 'editable-default', { selectedOptionLabel: 'CONST_CURRENT', editable: true }),
          this.createLegendCell(3, 'ENUM', 'CONST_VOLTAGE', 'user-modified', { selectedOptionLabel: 'CONST_VOLTAGE', editable: true, userModified: true }),
          this.createLegendCell(4, 'ENUM', 'CONST_VOLTAGE', 'computed', { selectedOptionLabel: 'CONST_VOLTAGE', computed: true }),
          this.createLegendCell(5, 'ENUM', 'CONST_CURRENT', 'computer-modified-after-computation', {
            selectedOptionLabel: 'CONST_CURRENT',
            editable: true,
            computed: true,
            computedFromModified: true
          })
        ]
      },
      {
        parameterName: 'Param 4',
        cells: [
          this.createLegendCell(1, 'ENUM', 'AUTO', 'golden-active', { selectedOptionLabel: 'AUTO' }),
          this.createLegendCell(2, 'ENUM', 'MANUAL', 'editable-default', { selectedOptionLabel: 'MANUAL', editable: true }),
          this.createLegendCell(3, 'ENUM', 'AUTO', 'golden-inactive', { selectedOptionLabel: 'AUTO', activationState: 'DISABLED' }),
          this.createLegendCell(4, 'ENUM', 'MANUAL', 'user-modified', { selectedOptionLabel: 'MANUAL', editable: true, userModified: true }),
          this.createLegendCell(5, 'ENUM', 'AUTO', 'computed', { selectedOptionLabel: 'AUTO', computed: true })
        ]
      }
    ];
  }

  private createLegendCell(
    stepId: number,
    valueType: RecipeMatrixCell['valueType'],
    displayValue: string,
    state: ColorLegendItem['cssClass'],
    overrides?: Partial<RecipeMatrixCell>
  ): RecipeMatrixCell {
    const defaultsByState: Record<ColorLegendItem['cssClass'], Partial<RecipeMatrixCell>> = {
      'golden-active': { lockedByGolden: true, activationState: 'ENABLED', editable: false, userModified: false, computed: false },
      'golden-inactive': { lockedByGolden: true, activationState: 'DISABLED', editable: false, userModified: false, computed: false },
      computed: { lockedByGolden: false, activationState: 'ENABLED', editable: false, userModified: false, computed: true },
      'editable-default': { lockedByGolden: false, activationState: 'ENABLED', editable: true, userModified: false, computed: false },
      'user-modified': { lockedByGolden: false, activationState: 'ENABLED', editable: true, userModified: true, computed: false },
      'computer-modified-after-computation': {
        lockedByGolden: false,
        activationState: 'ENABLED',
        editable: true,
        userModified: false,
        computed: true,
        computedFromModified: true
      },
      'readonly-neutral': { lockedByGolden: false, activationState: 'ENABLED', editable: false, userModified: false, computed: false }
    };

    const stateDefaults = defaultsByState[state];

    return {
      stepId,
      definitionId: stepId,
      valueType,
      displayValue,
      valueJson: null,
      selectedOptionId: null,
      selectedOptionLabel: null,
      availableOptions: [],
      lockedByGolden: false,
      editable: false,
      userModified: false,
      computed: false,
      ...stateDefaults,
      ...overrides,
      computedFromModified:
        overrides?.computedFromModified ??
        stateDefaults.computedFromModified ??
        false
    };
  }
}