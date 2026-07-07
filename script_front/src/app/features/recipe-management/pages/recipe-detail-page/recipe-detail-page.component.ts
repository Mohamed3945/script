import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Subscription, map } from 'rxjs';
import { ChamberCapability } from '../../../../core/models/chamber-capability.model';
import { ConfigurationDefinition } from '../../../../core/models/configuration-definition.model';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeCompatibilityResult } from '../../../../core/models/recipe-compatibility-result.model';
import { RecipeCompatibleMachineSearchRequest } from '../../../../core/models/recipe-compatible-machine-search-request.model';
import { RecipeKind } from '../../../../core/models/recipe-kind.model';
import { RecipeRequirements } from '../../../../core/models/recipe-requirements.model';
import { RecipeStatus } from '../../../../core/models/recipe-status.model';
import { RecipeMatrixCell } from '../../../../core/models/recipe-matrix-cell.model';
import { RecipeMatrix } from '../../../../core/models/recipe-matrix.model';
import { Step } from '../../../../core/models/step.model';
import { StepKind } from '../../../../core/models/step-kind.model';
import { StepParameter } from '../../../../core/models/step-parameter.model';
import { ChamberCapabilityApiService } from '../../../../core/services/chamber-capability-api.service';
import { ConfigurationDefinitionApiService } from '../../../../core/services/configuration-definition-api.service';
import { RecipeCompatibilityApiService } from '../../../../core/services/recipe-compatibility-api.service';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { RecipeBuilderService } from '../../../../core/services/recipe-builder.service';
import { RecipeRequirementsApiService } from '../../../../core/services/recipe-requirements-api.service';
import { RecipeDetailHeaderComponent } from '../../components/recipes/recipe-detail-header/recipe-detail-header.component';
import { RecipeRequirementsPanelComponent } from '../../components/recipes/recipe-requirements-panel/recipe-requirements-panel.component';
import { RecipeCompatibleMachinesPanelComponent } from '../../components/recipes/recipe-compatible-machines-panel/recipe-compatible-machines-panel.component';
import { SummaryViewComponent } from '../../components/recipes/summary-view/summary-view.component';
import { RecipeTabNavComponent, RecipeWorkspaceTab } from '../../components/recipes/recipe-tab-nav/recipe-tab-nav.component';
import { RecipeViewSwitchComponent, RecipeViewMode } from '../../components/recipes/recipe-view-switch/recipe-view-switch.component';
import { RecipeMatrixCellUpdate, RecipeMatrixViewComponent } from '../../components/recipes/recipe-matrix-view/recipe-matrix-view.component';
import { RecipeStepFocusViewComponent } from '../../components/recipes/recipe-step-focus-view/recipe-step-focus-view.component';
import {
  PrestepCellUpdate,
  RecipePrestepViewComponent
} from '../../components/recipes/recipe-prestep-view/recipe-prestep-view.component';
import { StepModalComponent } from '../../components/steps/step-modal/step-modal.component';
import { StepParameterModalComponent } from '../../components/steps/step-parameter-modal/step-parameter-modal.component';

@Component({
  selector: 'app-recipe-detail-page',
  standalone: true,
  imports: [
    NgIf,
    AsyncPipe,
    RecipeDetailHeaderComponent,
    RecipeRequirementsPanelComponent,
    RecipeCompatibleMachinesPanelComponent,
    SummaryViewComponent,
    RecipeTabNavComponent,
    RecipeViewSwitchComponent,
    RecipeMatrixViewComponent,
    RecipeStepFocusViewComponent,
    RecipePrestepViewComponent,
    StepModalComponent,
    StepParameterModalComponent
  ],
  templateUrl: './recipe-detail-page.component.html',
  styleUrl: './recipe-detail-page.component.scss'
})
/**
 * RecipeDetailPageComponent coordinates UI logic for this feature.
 */
export class RecipeDetailPageComponent implements OnInit, OnDestroy {
  workspaceMode: 'auto' | 'golden' | 'derived' = 'auto';
  recipe$ = new BehaviorSubject<Recipe | null>(null);
  compatibility$ = new BehaviorSubject<RecipeCompatibilityResult | null>(null);
  requirements$ = new BehaviorSubject<RecipeRequirements | null>(null);
  availableCapabilities$ = new BehaviorSubject<ChamberCapability[]>([]);
  availableConfigurationDefinitions$ = new BehaviorSubject<ConfigurationDefinition[]>([]);
  steps$;
  selectedStep$;
  stepParameters$;
  recipeMatrix$;
  loading$;
  stepsViewSteps$;
  stepsViewSelectedStep$;
  stepsViewMatrix$;
  prestepMatrix$;

  activeTab: RecipeWorkspaceTab = 'steps';
  viewMode: RecipeViewMode = 'matrix';
  showRightDrawer = true;

  showStepModal = false;
  showStepParameterModal = false;
  savingSummaryField: string | null = null;
  prestepStep: Step | null = null;
  modalStepId: number | null = null;
  modalStepKind: StepKind | null = null;
  modalParentCandidates: StepParameter[] = [];

  readonly recipeKindOptions: RecipeKind[] = ['GOLDEN', 'DERIVED', 'IMPORTED'];
  readonly recipeStatusOptions: RecipeStatus[] = ['DRAFT', 'VALIDATED', 'ARCHIVED'];

  private sub = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeApiService: RecipeApiService,
    private recipeBuilderService: RecipeBuilderService,
    private recipeRequirementsApiService: RecipeRequirementsApiService,
    private chamberCapabilityApiService: ChamberCapabilityApiService,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService,
    private recipeCompatibilityApiService: RecipeCompatibilityApiService
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

    this.prestepMatrix$ = this.recipeMatrix$.pipe(
      map((matrix) => this.buildPrestepMatrix(matrix))
    );

    this.sub.add(
      this.steps$.subscribe((steps) => {
        this.prestepStep = steps.find((step) => step.stepKind === 'PRESTEP') ?? null;
      })
    );
  }

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      return;
    }

    this.workspaceMode = this.route.snapshot.data['workspaceMode'] ?? 'auto';

    this.chamberCapabilityApiService.getCapabilities().subscribe({
      next: (capabilities) => this.availableCapabilities$.next(capabilities),
      error: (error) => console.error('Failed to load capabilities catalog', error)
    });

    this.configurationDefinitionApiService.getDefinitions().subscribe({
      next: (definitions) => this.availableConfigurationDefinitions$.next(definitions),
      error: (error) => console.error('Failed to load configuration definitions catalog', error)
    });

    const recipeSub = this.recipeApiService.getRecipeById(id).subscribe({
      next: (recipe) => {
        if (this.workspaceMode === 'auto') {
          this.workspaceMode = recipe.recipeKind === 'GOLDEN' ? 'golden' : 'derived';
        }
        this.recipe$.next(recipe);
        this.loadRequirementsForWorkspace(recipe);
        this.refreshCompatibilityByCapabilitiesOnly();
        this.recipeBuilderService.loadRecipeWorkspace(recipe);
      },
      error: (error) => {
        console.error('Failed to load recipe detail', error);
      }
    });

    this.sub.add(recipeSub);
  }

  /**
   * Handles the onEditRecipe workflow.
   */
  onEditRecipe(): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id) return;
    this.router.navigate(['/recipes', recipe.id, 'edit']);
  }

  /**
   * Handles the onDeleteRecipe workflow.
   */
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

  /**
   * Handles the onStepSelected workflow.
   */
  onStepSelected(step: Step): void {
    this.recipeBuilderService.setSelectedStep(step);
    if (step.id) {
      this.recipeBuilderService.loadStepParameters(step.id);
    }
  }

  /**
   * Handles the onCreateStep workflow.
   */
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

  /**
   * Handles the onCreateStepParameter workflow.
   */
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

  /**
   * Handles the onDeleteStepParameter workflow.
   */
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

  /**
   * Handles the onMatrixCellUpdated workflow.
   */
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

  /**
   * Handles the onPrestepCellUpdated workflow.
   */
  onPrestepCellUpdated(update: PrestepCellUpdate): void {
    this.onMatrixCellUpdated(update);
  }

  /**
   * Handles the onDeletePrestepParameter workflow.
   */
  onDeletePrestepParameter(cell: RecipeMatrixCell): void {
    if (!cell.stepParameterId) {
      return;
    }

    const confirmed = window.confirm(`Delete PRESTEP parameter #${cell.stepParameterId}?`);
    if (!confirmed) {
      return;
    }

    this.recipeApiService.deleteStepParameter(cell.stepParameterId).subscribe({
      next: () => {
        this.recipeBuilderService.refreshMatrix();
      },
      error: (error) => {
        console.error('Failed to delete PRESTEP parameter', error);
      }
    });
  }

  /**
   * Handles the openStepModal workflow.
   */
  openStepModal(): void {
    this.showStepModal = true;
  }

  toggleRightDrawer(): void {
    this.showRightDrawer = !this.showRightDrawer;
  }

  openContextualParameterModal(): void {
    if (this.activeTab === 'prestep') {
      this.openPrestepParameterModal();
      return;
    }

    this.openStepParameterModal();
  }

  get canAddParameter(): boolean {
    return this.activeTab === 'prestep'
      ? Boolean(this.prestepStep?.id)
      : Boolean(this.recipeBuilderService.selectedStepSnapshot);
  }

  get isDerivedWorkspace(): boolean {
    return this.workspaceMode === 'derived';
  }

  get workspaceHeading(): string {
    return this.isDerivedWorkspace ? 'Derived recipe customization' : 'Golden recipe workspace';
  }

  get workspaceDescription(): string {
    return this.isDerivedWorkspace
      ? 'Customize the inherited recipe and keep an eye on the compatible machine list in the right drawer.'
      : 'Manage the golden reference recipe structure and parameter baseline from this workspace.';
  }

  onSummaryFieldCommitted(event: { key: string; value: unknown }): void {
    this.commitSummaryPatch(event.key, event.value);
  }

  onAddRequiredCapability(capabilityId: number): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id || this.isDerivedWorkspace) return;

    this.recipeRequirementsApiService.addRequiredCapability(recipe.id, capabilityId).subscribe({
      next: (requirements) => {
        this.requirements$.next(requirements);
        this.refreshCompatibilityByCapabilitiesOnly();
      },
      error: (error) => console.error('Failed to add required capability', error)
    });
  }

  onRemoveRequiredCapability(capabilityId: number): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id || this.isDerivedWorkspace) return;

    this.recipeRequirementsApiService.removeRequiredCapability(recipe.id, capabilityId).subscribe({
      next: (requirements) => {
        this.requirements$.next(requirements);
        this.refreshCompatibilityByCapabilitiesOnly();
      },
      error: (error) => console.error('Failed to remove required capability', error)
    });
  }

  onAddRequiredConfiguration(configurationDefinitionId: number): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id || this.isDerivedWorkspace) return;

    this.recipeRequirementsApiService.addRequiredConfiguration(recipe.id, configurationDefinitionId).subscribe({
      next: (requirements) => {
        this.requirements$.next(requirements);
        this.refreshCompatibilityByCapabilitiesOnly();
      },
      error: (error) => console.error('Failed to add required configuration', error)
    });
  }

  onRemoveRequiredConfiguration(configurationDefinitionId: number): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id || this.isDerivedWorkspace) return;

    this.recipeRequirementsApiService.removeRequiredConfiguration(recipe.id, configurationDefinitionId).subscribe({
      next: (requirements) => {
        this.requirements$.next(requirements);
        this.refreshCompatibilityByCapabilitiesOnly();
      },
      error: (error) => console.error('Failed to remove required configuration', error)
    });
  }

  /**
   * Handles the closeStepModal workflow.
   */
  closeStepModal(): void {
    this.showStepModal = false;
  }

  /**
   * Handles the openStepParameterModal workflow.
   */
  openStepParameterModal(): void {
    const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
    if (!selectedStep) {
      return;
    }

    this.prepareStepParameterModal(selectedStep);
  }

  /**
   * Handles the openPrestepParameterModal workflow.
   */
  openPrestepParameterModal(): void {
    if (!this.prestepStep) {
      return;
    }

    this.prepareStepParameterModal(this.prestepStep);
  }

  /**
   * Handles the closeStepParameterModal workflow.
   */
  closeStepParameterModal(): void {
    this.showStepParameterModal = false;
    this.modalStepId = null;
    this.modalStepKind = null;
    this.modalParentCandidates = [];
  }

  /**
   * Handles the ngOnDestroy workflow.
   */
  ngOnDestroy(): void {
    this.sub.unsubscribe();
    this.recipeBuilderService.reset();
  }

  /**
   * Handles the filterMatrixToRegularSteps workflow.
   */
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
      .filter((row) => row.cells.some((cell) => Boolean(cell.stepParameterId)));

    return {
      ...matrix,
      columns: stepColumns,
      rows: filteredRows
    };
  }

  /**
   * Handles the buildPrestepRows workflow.
   */
  private buildPrestepMatrix(matrix: RecipeMatrix | null): RecipeMatrix | null {
    if (!matrix) {
      return null;
    }

    const prestepColumn = matrix.columns.find((column) => column.stepKind === 'PRESTEP');
    if (!prestepColumn) {
      return null;
    }

    const filteredRows = matrix.rows
      .map((row) => ({
        ...row,
        cells: row.cells.filter((candidate) => candidate.stepId === prestepColumn.stepId)
      }))
      .filter((row) => row.cells.some((cell) => Boolean(cell.stepParameterId)));

    return {
      ...matrix,
      columns: [prestepColumn],
      rows: filteredRows
    };
  }

  /**
   * Handles the prepareStepParameterModal workflow.
   */
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

  private loadRequirementsForWorkspace(recipe: Recipe): void {
    const parentRequirementsId = this.isDerivedWorkspace ? (recipe.parentRecipeId ?? null) : null;
    const primaryRequirementsId = parentRequirementsId ?? recipe.id ?? null;

    if (!primaryRequirementsId) {
      this.requirements$.next(this.buildEmptyRequirements(recipe));
      return;
    }

    this.recipeRequirementsApiService.getRequirements(primaryRequirementsId).subscribe({
      next: (requirements) => {
        this.requirements$.next(requirements);
        this.refreshCompatibilityByCapabilitiesOnly();
      },
      error: (primaryError) => {
        if (this.isDerivedWorkspace && recipe.id && primaryRequirementsId !== recipe.id) {
          this.recipeRequirementsApiService.getRequirements(recipe.id).subscribe({
            next: (requirements) => {
              this.requirements$.next(requirements);
              this.refreshCompatibilityByCapabilitiesOnly();
            },
            error: (fallbackError) => {
              console.error('Failed to load recipe requirements (parent and derived fallback)', {
                primaryError,
                fallbackError
              });
              this.requirements$.next(this.buildEmptyRequirements(recipe, primaryRequirementsId));
              this.refreshCompatibilityByCapabilitiesOnly();
            }
          });
          return;
        }

        console.error('Failed to load recipe requirements', primaryError);
        this.requirements$.next(this.buildEmptyRequirements(recipe, primaryRequirementsId));
        this.refreshCompatibilityByCapabilitiesOnly();
      }
    });
  }

  private buildEmptyRequirements(recipe: Recipe, recipeIdOverride?: number | null): RecipeRequirements {
    return {
      recipeId: recipeIdOverride ?? recipe.id ?? 0,
      recipeCode: null,
      recipeName: recipe.name,
      requiredCapabilities: [],
      requiredConfigurations: []
    };
  }

  refreshCompatibilityByCapabilitiesOnly(): void {
    const recipe = this.recipe$.value;
    const recipeId = recipe?.id ?? null;

    if (!this.isDerivedWorkspace || !recipeId) {
      this.compatibility$.next(null);
      return;
    }

    const request: RecipeCompatibleMachineSearchRequest = {
      recipeId,
      configurationConstraints: []
    };

    this.recipeCompatibilityApiService.findCompatibleMachines(request).subscribe({
      next: (result) => this.compatibility$.next(result),
      error: (error) => {
        console.error('Failed to compute compatible machines', error);
        this.compatibility$.next(this.buildEmptyCompatibility(recipeId));
      }
    });
  }

  private buildEmptyCompatibility(recipeId: number): RecipeCompatibilityResult {
    return {
      recipeId,
      compatibleMachineCount: 0,
      compatibleChamberCount: 0,
      machines: []
    };
  }

  private commitSummaryPatch(field: string, value: unknown): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id) {
      return;
    }

    const currentValue = (recipe as unknown as Record<string, unknown>)[field];
    if (currentValue === value) {
      return;
    }

    const payload = {
      ...recipe,
      [field]: value
    } as unknown as Recipe;

    this.savingSummaryField = String(field);
    this.recipeApiService.updateRecipe(recipe.id, payload).subscribe({
      next: (updatedRecipe) => {
        this.recipe$.next(updatedRecipe);
        this.savingSummaryField = null;
      },
      error: (error) => {
        console.error(`Failed to update recipe field ${String(field)}`, error);
        this.savingSummaryField = null;
      }
    });
  }
}
