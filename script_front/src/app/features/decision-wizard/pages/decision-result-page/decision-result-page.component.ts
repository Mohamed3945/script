import { AsyncPipe, NgClass, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { MachineChoice } from '../../../../core/models/machine-choice.model';
import { DecisionFinalizeRequest } from '../../../../core/models/decision-finalize-request.model';
import { RecipeMatrix } from '../../../../core/models/recipe-matrix.model';
import { RecipeMatrixCell } from '../../../../core/models/recipe-matrix-cell.model';
import { RecipeMatrixColumn } from '../../../../core/models/recipe-matrix-column.model';
import { RecipeMatrixRow } from '../../../../core/models/recipe-matrix-row.model';
import { RecipeRequirements } from '../../../../core/models/recipe-requirements.model';
import { RecipeCompatibilityResult } from '../../../../core/models/recipe-compatibility-result.model';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { RecipeRequirementsApiService } from '../../../../core/services/recipe-requirements-api.service';
import { RecipeCompatibilityApiService } from '../../../../core/services/recipe-compatibility-api.service';
import { DecisionWizardService } from '../../../../core/services/decision-wizard.service';
import { DecisionFinalizeApiService } from '../../../../core/services/decision-finalize-api.service';
import { buildRecipeDetailRouteByKind } from '../../../../core/utils/recipe-route.util';
import { ResultSummaryComponent } from '../../components/result-summary/result-summary.component';
import { ResultFinalActionsComponent } from '../../components/result-final-actions/result-final-actions.component';

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
  readonly previewStepCount = 10;
  readonly previewParamCount = 20;

  private readonly wizardService = inject(DecisionWizardService);
  private readonly decisionFinalizeApiService = inject(DecisionFinalizeApiService);
  private readonly recipeApiService = inject(RecipeApiService);
  private readonly recipeRequirementsApiService = inject(RecipeRequirementsApiService);
  private readonly recipeCompatibilityApiService = inject(RecipeCompatibilityApiService);
  private readonly router = inject(Router);

  result$ = this.wizardService.result$;
  selections$ = this.wizardService.selections$;

  loading$ = new BehaviorSubject<boolean>(false);
  goldenMatrix$ = new BehaviorSubject<RecipeMatrix | null>(null);
  requirements$ = new BehaviorSubject<RecipeRequirements | null>(null);
  compatibility$ = new BehaviorSubject<RecipeCompatibilityResult | null>(null);

  goldenValidated = false;
  machineChoices: MachineChoice[] = [];
  selectedMachineId: number | null = null;
  loadingPreview = false;

  constructor() {
    const result = this.wizardService.resultSnapshot ?? null;
    if (result?.goldenRecipeId) {
      this.loadGoldenPreview(result.goldenRecipeId, result.machineId ?? null);
    }
  }

  get canSubmit(): boolean {
    return this.goldenValidated && Boolean(this.selectedMachineId);
  }

  get previewColumns(): RecipeMatrixColumn[] {
    const matrix = this.goldenMatrix$.value;
    if (!matrix) {
      return [];
    }
    return [...matrix.columns]
      .sort((a, b) => a.orderIndex - b.orderIndex)
      .slice(0, this.previewStepCount);
  }

  get previewRows(): RecipeMatrixRow[] {
    const matrix = this.goldenMatrix$.value;
    if (!matrix) {
      return [];
    }
    return matrix.rows.slice(0, this.previewParamCount);
  }

  getPreviewCell(row: RecipeMatrixRow, column: RecipeMatrixColumn): RecipeMatrixCell | null {
    return row.cells.find((cell) => cell.stepId === column.stepId) ?? null;
  }

  getPreviewCellValue(cell: RecipeMatrixCell | null): string {
    if (!cell) {
      return '—';
    }
    return cell.selectedOptionLabel || cell.displayValue || '—';
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

  private loadGoldenPreview(goldenRecipeId: number, preferredMachineId: number | null): void {
    this.loadingPreview = true;

    this.recipeApiService.getRecipeMatrix(goldenRecipeId).subscribe({
      next: (matrix) => this.goldenMatrix$.next(matrix),
      error: (error) => console.error('Failed to load golden matrix preview', error)
    });

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
          this.loadingPreview = false;
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
          this.loadingPreview = false;
        }
      });
  }
}