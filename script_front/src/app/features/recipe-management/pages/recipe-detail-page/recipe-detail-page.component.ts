import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Subscription, map } from 'rxjs';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeMatrixCell } from '../../../../core/models/recipe-matrix-cell.model';
import { RecipeMatrix } from '../../../../core/models/recipe-matrix.model';
import { Step } from '../../../../core/models/step.model';
import { StepKind } from '../../../../core/models/step-kind.model';
import { StepParameter } from '../../../../core/models/step-parameter.model';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { RecipeBuilderService } from '../../../../core/services/recipe-builder.service';
import { RecipeDetailHeaderComponent } from '../../components/recipes/recipe-detail-header/recipe-detail-header.component';
import { RecipeSummarySidecardComponent } from '../../components/recipes/recipe-summary-sidecard/recipe-summary-sidecard.component';
import { RecipeTabNavComponent, RecipeWorkspaceTab } from '../../components/recipes/recipe-tab-nav/recipe-tab-nav.component';
import { RecipeViewSwitchComponent, RecipeViewMode } from '../../components/recipes/recipe-view-switch/recipe-view-switch.component';
import { RecipeMatrixCellUpdate, RecipeMatrixViewComponent } from '../../components/recipes/recipe-matrix-view/recipe-matrix-view.component';
import { RecipeStepFocusViewComponent } from '../../components/recipes/recipe-step-focus-view/recipe-step-focus-view.component';
import { StepModalComponent } from '../../components/steps/step-modal/step-modal.component';
import { StepParameterModalComponent } from '../../components/steps/step-parameter-modal/step-parameter-modal.component';

@Component({
  selector: 'app-recipe-detail-page',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    AsyncPipe,
    RecipeDetailHeaderComponent,
    RecipeSummarySidecardComponent,
    RecipeTabNavComponent,
    RecipeViewSwitchComponent,
    RecipeMatrixViewComponent,
    RecipeStepFocusViewComponent,
    StepModalComponent,
    StepParameterModalComponent
  ],
  templateUrl: './recipe-detail-page.component.html',
  styleUrl: './recipe-detail-page.component.scss'
})
export class RecipeDetailPageComponent implements OnInit, OnDestroy {
  recipe$ = new BehaviorSubject<Recipe | null>(null);
  steps$;
  selectedStep$;
  stepParameters$;
  recipeMatrix$;
  loading$;
  stepsViewSteps$;
  stepsViewSelectedStep$;
  stepsViewMatrix$;
  prestepRows$;

  activeTab: RecipeWorkspaceTab = 'steps';
  viewMode: RecipeViewMode = 'matrix';

  showStepModal = false;
  showStepParameterModal = false;
  prestepStep: Step | null = null;
  modalStepId: number | null = null;
  modalStepKind: StepKind | null = null;
  modalParentCandidates: StepParameter[] = [];

  private sub = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeApiService: RecipeApiService,
    private recipeBuilderService: RecipeBuilderService
  ) {
    this.steps$ = this.recipeBuilderService.steps$;
    this.selectedStep$ = this.recipeBuilderService.selectedStep$;
    this.stepParameters$ = this.recipeBuilderService.stepParameters$;
    this.recipeMatrix$ = this.recipeBuilderService.recipeMatrix$;
    this.loading$ = this.recipeBuilderService.loading$;

    this.stepsViewSteps$ = this.steps$.pipe(
      map((steps) => steps.filter((step) => step.stepKind !== 'PRESTEP'))
    );

    this.stepsViewSelectedStep$ = this.selectedStep$.pipe(
      map((step) => (step?.stepKind === 'PRESTEP' ? null : step))
    );

    this.stepsViewMatrix$ = this.recipeMatrix$.pipe(
      map((matrix) => this.filterMatrixToRegularSteps(matrix))
    );

    this.prestepRows$ = this.recipeMatrix$.pipe(
      map((matrix) => this.buildPrestepRows(matrix))
    );

    this.sub.add(
      this.steps$.subscribe((steps) => {
        this.prestepStep = steps.find((step) => step.stepKind === 'PRESTEP') ?? null;
      })
    );
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      return;
    }

    const recipeSub = this.recipeApiService.getRecipeById(id).subscribe({
      next: (recipe) => {
        this.recipe$.next(recipe);
        this.recipeBuilderService.loadRecipeWorkspace(recipe);
      },
      error: (error) => {
        console.error('Failed to load recipe detail', error);
      }
    });

    this.sub.add(recipeSub);
  }

  onEditRecipe(): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id) return;
    this.router.navigate(['/recipes', recipe.id, 'edit']);
  }

  onDeleteRecipe(): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id) return;

    const confirmed = window.confirm(`Delete recipe "${recipe.name}"?`);
    if (!confirmed) return;

    this.recipeApiService.deleteRecipe(recipe.id).subscribe({
      next: () => this.router.navigate(['/recipes']),
      error: (error) => console.error('Failed to delete recipe', error)
    });
  }

  onStepSelected(step: Step): void {
    this.recipeBuilderService.setSelectedStep(step);
    if (step.id) {
      this.recipeBuilderService.loadStepParameters(step.id);
    }
  }

  onCreateStep(step: Step): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id) return;

    this.recipeApiService.createStep(recipe.id, step).subscribe({
      next: () => {
        this.showStepModal = false;
        this.recipeBuilderService.loadRecipeWorkspace(recipe);
      },
      error: (error) => {
        console.error('Failed to create step', error);
      }
    });
  }

  onCreateStepParameter(payload: StepParameter): void {
    if (!this.modalStepId) {
      return;
    }

    this.recipeApiService.createStepParameter(this.modalStepId, payload).subscribe({
      next: () => {
        const modalStepId = this.modalStepId;
        const selectedStep = this.recipeBuilderService.selectedStepSnapshot;

        this.closeStepParameterModal();

        if (selectedStep?.id === modalStepId) {
          this.recipeBuilderService.loadStepParameters(selectedStep.id);
        }

        this.recipeBuilderService.refreshMatrix();
      },
      error: (error) => {
        console.error('Failed to create step parameter', error);
      }
    });
  }

  onDeleteStepParameter(parameter: StepParameter): void {
    if (!parameter.id) return;

    const confirmed = window.confirm(`Delete step parameter #${parameter.id}?`);
    if (!confirmed) return;

    this.recipeApiService.deleteStepParameter(parameter.id).subscribe({
      next: () => {
        const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
        if (selectedStep?.id) {
          this.recipeBuilderService.loadStepParameters(selectedStep.id);
        }
        this.recipeBuilderService.refreshMatrix();
      },
      error: (error) => console.error('Failed to delete step parameter', error)
    });
  }

  onMatrixCellUpdated(update: RecipeMatrixCellUpdate): void {
    const cell: RecipeMatrixCell = update.cell;
    if (!cell.stepParameterId) {
      return;
    }

    const payload: StepParameter = {
      definitionId: cell.definitionId,
      valueJson: update.valueType === 'ENUM' ? null : (update.valueJson ?? null),
      selectedOptionId: update.valueType === 'ENUM' ? (update.selectedOptionId ?? null) : null,
      lockedByGolden: cell.lockedByGolden
    };

    this.recipeApiService.updateStepParameter(cell.stepParameterId, payload).subscribe({
      next: () => {
        this.recipeBuilderService.refreshMatrix();

        const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
        if (selectedStep?.id === cell.stepId) {
          this.recipeBuilderService.loadStepParameters(selectedStep.id);
        }
      },
      error: (error) => {
        console.error('Failed to update matrix cell', error);
      }
    });
  }

  openStepModal(): void {
    this.showStepModal = true;
  }

  closeStepModal(): void {
    this.showStepModal = false;
  }

  openStepParameterModal(): void {
    const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
    if (!selectedStep) {
      return;
    }

    this.prepareStepParameterModal(selectedStep);
  }

  openPrestepParameterModal(): void {
    if (!this.prestepStep) {
      return;
    }

    this.prepareStepParameterModal(this.prestepStep);
  }

  closeStepParameterModal(): void {
    this.showStepParameterModal = false;
    this.modalStepId = null;
    this.modalStepKind = null;
    this.modalParentCandidates = [];
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    this.recipeBuilderService.reset();
  }

  private filterMatrixToRegularSteps(matrix: RecipeMatrix | null): RecipeMatrix | null {
    if (!matrix) {
      return null;
    }

    const stepColumns = matrix.columns.filter((column) => column.stepKind !== 'PRESTEP');
    const stepColumnIds = new Set(stepColumns.map((column) => column.stepId));

    const filteredRows = matrix.rows
      .map((row) => ({
        ...row,
        cells: row.cells.filter((cell) => stepColumnIds.has(cell.stepId))
      }))
      .filter((row) => row.cells.length > 0);

    return {
      ...matrix,
      columns: stepColumns,
      rows: filteredRows
    };
  }

  private buildPrestepRows(matrix: RecipeMatrix | null): Array<{ parameter: string; value: string }> {
    if (!matrix) {
      return [];
    }

    const prestepColumn = matrix.columns.find((column) => column.stepKind === 'PRESTEP');
    if (!prestepColumn) {
      return [];
    }

    return matrix.rows
      .map((row) => {
        const cell = row.cells.find((candidate) => candidate.stepId === prestepColumn.stepId);
        if (!cell?.stepParameterId) {
          return null;
        }

        const value = cell.selectedOptionLabel ?? cell.valueJson ?? cell.displayValue ?? '-';
        return {
          parameter: row.parameterAlias || row.parameterName,
          value
        };
      })
      .filter((row): row is { parameter: string; value: string } => row !== null);
  }

  private prepareStepParameterModal(step: Step): void {
    if (!step.id) {
      return;
    }

    this.modalStepId = step.id;
    this.modalStepKind = step.stepKind;
    this.modalParentCandidates = [];
    this.showStepParameterModal = true;

    this.recipeApiService.getStepParameters(step.id).subscribe({
      next: (parameters) => {
        if (this.modalStepId === step.id) {
          this.modalParentCandidates = parameters;
        }
      },
      error: (error) => {
        console.error('Failed to load step parameters for modal', error);
        if (this.modalStepId === step.id) {
          this.modalParentCandidates = [];
        }
      }
    });
  }
}