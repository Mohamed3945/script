import { AsyncPipe, NgIf } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {BehaviorSubject,Subject,Subscription,distinctUntilChanged,filter,finalize,map,takeUntil} from 'rxjs';
import { ChamberCapability } from '../../../../core/models/chamber-capability.model';
import { ChamberDetail } from '../../../../core/models/chamber-detail.model';
import { ConfigurationDefinition } from '../../../../core/models/configuration-definition.model';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeCompatibilityResult } from '../../../../core/models/recipe-compatibility-result.model';
import { RecipeCompatibleMachineSearchRequest } from '../../../../core/models/recipe-compatible-machine-search-request.model';
import { RecipeCustomizationSummary } from '../../../../core/models/recipe-customization-summary.model';
import { RecipeKind } from '../../../../core/models/recipe-kind.model';
import { RecipeRequirements } from '../../../../core/models/recipe-requirements.model';
import { RecipeStatus } from '../../../../core/models/recipe-status.model';
import { RecipeMatrixCell } from '../../../../core/models/recipe-matrix-cell.model';
import { RecipeMatrixEndpointCell } from '../../../../core/models/recipe-matrix-endpoint-cell.model';
import { RecipeMatrix } from '../../../../core/models/recipe-matrix.model';
import { Step } from '../../../../core/models/step.model';
import { StepKind } from '../../../../core/models/step-kind.model';
import { StepParameter } from '../../../../core/models/step-parameter.model';
import { ChamberCapabilityApiService } from '../../../../core/services/chamber-capability-api.service';
import { ChamberApiService } from '../../../../core/services/chamber-api.service';
import { ConfigurationDefinitionApiService } from '../../../../core/services/configuration-definition-api.service';
import { ParameterDefinitionApiService } from '../../../../core/services/parameter-definition-api.service';
import { RecipeCompatibilityApiService } from '../../../../core/services/recipe-compatibility-api.service';
import { RecipeCompatibilityRefreshService } from '../../../../core/services/recipe-compatibility-refresh.service';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { RecipeBuilderService } from '../../../../core/services/recipe-builder.service';
import { RecipeRequirementsApiService } from '../../../../core/services/recipe-requirements-api.service';
import { RecipeDetailHeaderComponent } from '../../components/recipes/recipe-detail-header/recipe-detail-header.component';
import { RecipeXmlExportReviewModalComponent } from '../../components/recipes/recipe-xml-export-review-modal/recipe-xml-export-review-modal.component';
import { RecipeRequirementsPanelComponent } from '../../components/recipes/recipe-requirements-panel/recipe-requirements-panel.component';
import { RecipeCompatibleMachinesPanelComponent } from '../../components/recipes/recipe-compatible-machines-panel/recipe-compatible-machines-panel.component';
import { SummaryViewComponent } from '../../components/recipes/summary-view/summary-view.component';
import { RecipeTabNavComponent, RecipeWorkspaceTab } from '../../components/recipes/recipe-tab-nav/recipe-tab-nav.component';
import { RecipeViewSwitchComponent, RecipeViewMode } from '../../components/recipes/recipe-view-switch/recipe-view-switch.component';
import {
  RecipeMatrixCellContextRequest,
  RecipeMatrixEndpointCellContextRequest,
  RecipeMatrixCellUpdate,
  RecipeMatrixParameterRowContextRequest,
  RecipeMatrixStepContextRequest,
  RecipeMatrixViewComponent
} from '../../components/recipes/recipe-matrix-view/recipe-matrix-view.component';
import { RecipeStepFocusViewComponent } from '../../components/recipes/recipe-step-focus-view/recipe-step-focus-view.component';
import {
  PrestepCellContextRequest,
  PrestepCellUpdate,
  PrestepParameterRowContextRequest,
  RecipePrestepViewComponent
} from '../../components/recipes/recipe-prestep-view/recipe-prestep-view.component';
import { StepModalComponent } from '../../components/steps/step-modal/step-modal.component';
import { StepEndpointModalComponent } from '../../components/steps/step-endpoint-modal/step-endpoint-modal.component';
import { StepParameterModalComponent } from '../../components/steps/step-parameter-modal/step-parameter-modal.component';

@Component({
  selector: 'app-recipe-detail-page',
  standalone: true,
  imports: [
    NgIf,
    AsyncPipe,
    RecipeDetailHeaderComponent,
    RecipeXmlExportReviewModalComponent,
    RecipeRequirementsPanelComponent,
    RecipeCompatibleMachinesPanelComponent,
    SummaryViewComponent,
    RecipeTabNavComponent,
    RecipeViewSwitchComponent,
    RecipeMatrixViewComponent,
    RecipeStepFocusViewComponent,
    RecipePrestepViewComponent,
    StepModalComponent,
    StepEndpointModalComponent,
    StepParameterModalComponent
  ],
  templateUrl: './recipe-detail-page.component.html',
  styleUrl: './recipe-detail-page.component.scss'
})
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
  showEndpointModal = false;
  showXmlExportReviewModal = false;
  xmlExportDownloading = false;
  xmlExportCustomizationSummary: RecipeCustomizationSummary | null = null;
  xmlExportLoadingCustomizationSummary = false;
  xmlExportSelectedMachineId: number | null = null;
  xmlExportSelectedChamberId: number | null = null;
  xmlExportSelectedChamberDetail: ChamberDetail | null = null;
  xmlExportLoadingChamberDetail = false;
  xmlExportUnderstandChecked = false;
  savingSummaryField: string | null = null;
  prestepStep: Step | null = null;
  modalStepId: number | null = null;
  modalStepKind: StepKind | null = null;
  modalParentCandidates: StepParameter[] = [];
  modalExcludedDefinitionIds: number[] = [];
  endpointModalStepId: number | null = null;
  endpointModalLockedByGolden = false;

  stepContextMenu: { visible: boolean; x: number; y: number; stepId: number | null; stepCode: string | null } = {
    visible: false,
    x: 0,
    y: 0,
    stepId: null,
    stepCode: null
  };

  cellActionBar: {
    visible: boolean;
    x: number;
    y: number;
    cell: RecipeMatrixCell | null;
    endpointCell: RecipeMatrixEndpointCell | null;
  } = {
    visible: false,
    x: 0,
    y: 0,
    cell: null,
    endpointCell: null
  };

  parameterRowContextMenu: {
    visible: boolean;
    x: number;
    y: number;
    definitionId: number | null;
    parameterName: string | null;
  } = {
    visible: false,
    x: 0,
    y: 0,
    definitionId: null,
    parameterName: null
  };

  showParameterDetailsModal = false;
  parameterDetailsLoading = false;
  parameterDetailsName = '';
  parameterDetailsCode: string | null = null;
  parameterDetailsAlias: string | null = null;
  parameterDetailsDescription: string | null = null;

  compatibility: RecipeCompatibilityResult | null = null;
  compatibilityLoading = false;
  compatibilityFilteringFeedbackActive = false;
  compatibilityFilteringFeedbackTick = 0;
  recomputedPulseStepParameterIds: number[] = [];
  recomputedPulseTick = 0;

  readonly recipeKindOptions: RecipeKind[] = ['GOLDEN', 'DERIVED', 'IMPORTED'];
  readonly recipeStatusOptions: RecipeStatus[] = ['DRAFT', 'VALIDATED', 'ARCHIVED'];

  private sub = new Subscription();
  private readonly destroy$ = new Subject<void>();
  private currentRecipeId: number | null = null;
  private compatibilityFeedbackToken = 0;
  private compatibilityFeedbackTimeoutId: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
    private recipeApiService: RecipeApiService,
    private recipeBuilderService: RecipeBuilderService,
    private recipeRequirementsApiService: RecipeRequirementsApiService,
    private chamberCapabilityApiService: ChamberCapabilityApiService,
    private chamberApiService: ChamberApiService,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService,
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private recipeCompatibilityApiService: RecipeCompatibilityApiService,
    private recipeCompatibilityRefreshService: RecipeCompatibilityRefreshService
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

    this.sub.add(
      this.recipeMatrix$.subscribe((matrix) => {
        const previousMatrix = this.latestRecipeMatrix;
        this.latestRecipeMatrix = matrix;
        this.updateRecomputedPulseFromMatrixDelta(previousMatrix, matrix);
      })
    );
  }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      map(params => Number(params.get('id'))),
      filter(id => !Number.isNaN(id)),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(recipeId => {
      this.currentRecipeId = recipeId;
    });

    this.recipeCompatibilityRefreshService.refresh$
      .pipe(takeUntil(this.destroy$))
      .subscribe(recipeId => {
        this.refreshCompatibility(recipeId ?? this.currentRecipeId);
      });

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
        this.currentRecipeId = recipe.id ?? id;
        this.loadRequirementsForWorkspace(recipe);
        this.recipeBuilderService.loadRecipeWorkspace(recipe);
      },
      error: (error) => {
        console.error('Failed to load recipe detail', error);
      }
    });

    this.sub.add(recipeSub);
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.closeAllContextActions();
  }

  @HostListener('document:scroll')
  onDocumentScroll(): void {
    this.closeAllContextActions();
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

  onExportXmlRequested(): void {
    if (this.isDerivedWorkspace) {
      this.openXmlExportReviewModal();
      return;
    }

    this.downloadXmlForCurrentSelectionOrDefault();
  }

  onXmlExportMachineChanged(machineId: number | null): void {
    this.xmlExportSelectedMachineId = machineId;
    this.xmlExportUnderstandChecked = false;
    this.xmlExportSelectedChamberDetail = null;

    if (machineId == null) {
      this.xmlExportSelectedChamberId = null;
      return;
    }

    const selectedMachine = this.compatibility?.machines
      ?.find((machine) => machine.machineId === machineId);
    const firstChamberId = selectedMachine?.compatibleChambers?.[0]?.chamberId ?? null;
    this.onXmlExportChamberChanged(firstChamberId);
  }

  onXmlExportChamberChanged(chamberId: number | null): void {
    this.xmlExportSelectedChamberId = chamberId;
    this.xmlExportUnderstandChecked = false;
    this.xmlExportSelectedChamberDetail = null;

    if (chamberId == null) {
      return;
    }

    this.xmlExportLoadingChamberDetail = true;
    this.chamberApiService.getChamber(chamberId).subscribe({
      next: (detail) => {
        if (this.xmlExportSelectedChamberId === chamberId) {
          this.xmlExportSelectedChamberDetail = detail;
        }
        this.xmlExportLoadingChamberDetail = false;
      },
      error: (error) => {
        console.error('Failed to load chamber detail for XML export review', error);
        this.xmlExportLoadingChamberDetail = false;
      }
    });
  }

  onXmlExportUnderstandChanged(checked: boolean): void {
    this.xmlExportUnderstandChecked = checked;
  }

  onXmlExportDownloadConfirmed(): void {
    if (!this.xmlExportUnderstandChecked) {
      return;
    }

    this.downloadXmlForCurrentSelectionOrDefault();
  }

  closeXmlExportReviewModal(): void {
    this.showXmlExportReviewModal = false;
    this.xmlExportCustomizationSummary = null;
    this.xmlExportLoadingCustomizationSummary = false;
    this.xmlExportSelectedMachineId = null;
    this.xmlExportSelectedChamberId = null;
    this.xmlExportSelectedChamberDetail = null;
    this.xmlExportLoadingChamberDetail = false;
    this.xmlExportUnderstandChecked = false;
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

    this.recipeApiService.createStructuredStep(recipe.id, {
      name: step.name,
      stepKind: step.stepKind,
      orderIndex: step.orderIndex ?? null
    }).subscribe({
      next: () => {
        this.showStepModal = false;
        this.runWithViewportPreserved(() => {
          this.recipeBuilderService.loadRecipeWorkspace(recipe);
        });
        this.requestCompatibilityRefresh();
      },
      error: (error) => {
        console.error('Failed to create structured step', error);
      }
    });
  }

  onCreateStepParameter(payload: StepParameter): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id || !this.modalStepKind) {
      return;
    }

    const stepKind: StepKind = this.modalStepKind === 'PRESTEP' ? 'PRESTEP' : 'STEP';
    const request = this.buildStepParameterCreatePayload(payload);

    this.recipeApiService.propagateStepParameter(recipe.id, stepKind, request).subscribe({
      next: () => {
        const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
        this.closeStepParameterModal();

        if (selectedStep?.id) {
          this.recipeBuilderService.loadStepParameters(selectedStep.id);
        }
        this.recipeBuilderService.refreshMatrix();
        this.requestCompatibilityRefresh();
      },
      error: (error) => {
        console.error('Failed to propagate recipe-wide step parameter(s)', error);
      }
    });
  }

  onDeleteStepParameter(parameter: StepParameter): void {
    if (!parameter.id) return;

    const confirmed = window.confirm(`Delete step parameter #${parameter.id}?`);
    if (!confirmed) return;

    this.recipeApiService.deleteStepParameter(parameter.id).subscribe({
      next: () => {
        this.runWithViewportPreserved(() => {
          const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
          if (selectedStep?.id) {
            this.recipeBuilderService.loadStepParameters(selectedStep.id);
          }
          this.recipeBuilderService.refreshMatrix();
        });
        this.requestCompatibilityRefresh();
      },
      error: (error) => console.error('Failed to delete step parameter', error)
    });
  }

  onMatrixCellUpdated(update: RecipeMatrixCellUpdate): void {
    const cell: RecipeMatrixCell = update.cell;
    if (!cell.stepParameterId) {
      return;
    }
    if (update.action === 'toggle-golden') {
      if (!this.isGoldenWorkspace) {
        return;
      }

      const togglePayload: StepParameter = {
        definitionId: cell.definitionId,
        valueJson: cell.valueJson ?? null,
        selectedOptionId: cell.selectedOptionId ?? null,
        lockedByGolden: update.lockedByGolden ?? !cell.lockedByGolden
      };

      this.recipeApiService.updateStepParameter(cell.stepParameterId, togglePayload).subscribe({
        next: () => {
          this.runWithViewportPreserved(() => {
            this.recipeBuilderService.refreshMatrix();
            this.closeCellActionBar();
          });
          this.requestCompatibilityRefresh();
        },
        error: (error) => console.error('Failed to toggle golden lock', error)
      });
      return;
    }

    if (this.isDerivedWorkspace && cell.lockedByGolden) {
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
        this.runWithViewportPreserved(() => {
          this.recipeBuilderService.refreshMatrix();

          const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
          if (selectedStep?.id === cell.stepId) {
            this.recipeBuilderService.loadStepParameters(selectedStep.id);
          }
        });
        this.requestCompatibilityRefresh(this.isDerivedWorkspace ? 'sp-change' : 'other');
      },
      error: (error) => {
        console.error('Failed to update matrix cell', error);
      }
    });
  }

  onPrestepCellUpdated(update: PrestepCellUpdate): void {
    this.onMatrixCellUpdated(update);
  }

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
        this.runWithViewportPreserved(() => {
          this.recipeBuilderService.refreshMatrix();
        });
        this.requestCompatibilityRefresh(this.isDerivedWorkspace ? 'sp-change' : 'other');
      },
      error: (error) => {
        console.error('Failed to delete PRESTEP parameter', error);
      }
    });
  }

  onStepHeaderContextRequested(request: RecipeMatrixStepContextRequest): void {
    if (!this.isGoldenWorkspace) {
      return;
    }

    this.closeCellActionBar();
    this.closeParameterRowContextMenu();

    this.stepContextMenu = {
      visible: true,
      x: request.x,
      y: request.y,
      stepId: request.stepId,
      stepCode: request.stepCode ?? null
    };
  }

  onMatrixCellContextRequested(request: RecipeMatrixCellContextRequest): void {
    if (!this.isGoldenWorkspace) {
      return;
    }

    this.closeStepContextMenu();
    this.closeParameterRowContextMenu();

    this.cellActionBar = {
      visible: true,
      x: request.x,
      y: request.y,
      cell: request.cell,
      endpointCell: null
    };
  }

  onPrestepCellContextRequested(request: PrestepCellContextRequest): void {
    if (!this.isGoldenWorkspace) {
      return;
    }

    this.closeStepContextMenu();
    this.closeParameterRowContextMenu();

    this.cellActionBar = {
      visible: true,
      x: request.x,
      y: request.y,
      cell: request.cell,
      endpointCell: null
    };
  }

  onParameterRowContextRequested(request: RecipeMatrixParameterRowContextRequest): void {
    this.closeStepContextMenu();
    this.closeCellActionBar();

    this.parameterRowContextMenu = {
      visible: true,
      x: request.x,
      y: request.y,
      definitionId: request.definitionId,
      parameterName: request.parameterName
    };
  }

  onEndpointCellContextRequested(request: RecipeMatrixEndpointCellContextRequest): void {
    if (!this.isGoldenWorkspace) {
      return;
    }

    this.closeStepContextMenu();
    this.closeParameterRowContextMenu();

    this.cellActionBar = {
      visible: true,
      x: request.x,
      y: request.y,
      cell: null,
      endpointCell: request.endpointCell
    };
  }

  onPrestepParameterRowContextRequested(request: PrestepParameterRowContextRequest): void {
    this.closeStepContextMenu();
    this.closeCellActionBar();

    this.parameterRowContextMenu = {
      visible: true,
      x: request.x,
      y: request.y,
      definitionId: request.definitionId,
      parameterName: request.parameterName
    };
  }

  onDeleteStepFromContext(): void {
    const stepId = this.stepContextMenu.stepId;
    if (!stepId || !this.isGoldenWorkspace) {
      return;
    }

    const confirmed = window.confirm(`Delete step ${this.stepContextMenu.stepCode ?? '#' + stepId}?`);
    if (!confirmed) {
      return;
    }

    this.recipeApiService.deleteStep(stepId).subscribe({
      next: () => {
        this.closeStepContextMenu();
        const recipe = this.recipe$.value;
        if (!recipe) {
          return;
        }
        this.runWithViewportPreserved(() => {
          this.recipeBuilderService.loadRecipeWorkspace(recipe);
        });
        this.requestCompatibilityRefresh();
      },
      error: (error) => console.error('Failed to delete step', error)
    });
  }

  onToggleGoldenFromContext(): void {
    const cell = this.cellActionBar.cell;
    const endpointCell = this.cellActionBar.endpointCell;

    if (!this.isGoldenWorkspace) {
      return;
    }

    if (endpointCell?.stepId) {
      this.recipeApiService.getStepEndpoint(endpointCell.stepId).subscribe({
        next: (endpoint) => {
          const payload = {
            ...endpoint,
            stepId: endpointCell.stepId,
            lockedByGolden: !endpointCell.lockedByGolden
          };

          this.recipeApiService.upsertStepEndpoint(endpointCell.stepId, payload).subscribe({
            next: () => {
              this.runWithViewportPreserved(() => {
                this.recipeBuilderService.refreshMatrix();
                this.closeCellActionBar();
              });
              this.requestCompatibilityRefresh();
            },
            error: (error) => console.error('Failed to toggle endpoint golden lock', error)
          });
        },
        error: (error) => console.error('Failed to load endpoint before lock toggle', error)
      });
      return;
    }

    if (!cell?.stepParameterId) {
      return;
    }

    this.onMatrixCellUpdated({
      cell,
      valueType: cell.valueType,
      action: 'toggle-golden',
      lockedByGolden: !cell.lockedByGolden
    });
  }

  onEditEndpointFromContext(): void {
    const endpointCell = this.cellActionBar.endpointCell;
    if (!endpointCell?.stepId) {
      return;
    }

    this.closeCellActionBar();
    this.openEndpointModalForCell(endpointCell);
  }

  onDeleteRecipeWideParameterFromContext(): void {
    const recipe = this.recipe$.value;
    const definitionId = this.parameterRowContextMenu.definitionId;

    if (!recipe?.id || !definitionId || !this.isGoldenWorkspace) {
      return;
    }

    const confirmed = window.confirm(
      `Delete parameter "${this.parameterRowContextMenu.parameterName ?? ('#' + definitionId)}" from the whole recipe?`
    );
    if (!confirmed) {
      return;
    }

    this.recipeApiService.deleteRecipeWideParameter(recipe.id, definitionId).subscribe({
      next: () => {
        this.closeParameterRowContextMenu();
        this.runWithViewportPreserved(() => {
          this.recipeBuilderService.refreshMatrix();

          const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
          if (selectedStep?.id) {
            this.recipeBuilderService.loadStepParameters(selectedStep.id);
          }
        });
        this.requestCompatibilityRefresh();
      },
      error: (error) => {
        console.error('Failed to delete recipe-wide parameter', error);
      }
    });
  }

  onShowParameterDetailsFromContext(): void {
    const definitionId = this.parameterRowContextMenu.definitionId;
    if (!definitionId) {
      return;
    }

    const fallbackName = this.parameterRowContextMenu.parameterName;
    this.closeParameterRowContextMenu();
    this.openParameterDetailsModal(definitionId, fallbackName);
  }

  closeParameterDetailsModal(): void {
    this.showParameterDetailsModal = false;
    this.parameterDetailsLoading = false;
    this.parameterDetailsName = '';
    this.parameterDetailsCode = null;
    this.parameterDetailsAlias = null;
    this.parameterDetailsDescription = null;
  }

  openStepModal(): void {
    if (!this.isGoldenWorkspace) {
      return;
    }
    this.showStepModal = true;
  }

  openComputationFormulasPage(): void {
    const recipe = this.recipe$.value;
    if (!this.isGoldenWorkspace || !recipe?.id) {
      return;
    }

    this.router.navigate(['/recipes/golden', recipe.id, 'formulas']);
  }

  toggleRightDrawer(): void {
    this.showRightDrawer = !this.showRightDrawer;
    this.closeAllContextActions();
  }

  get canAddParameter(): boolean {
    return this.activeTab === 'prestep'
      ? Boolean(this.prestepStep?.id)
      : this.recipeBuilderService.stepsSnapshot.some((s) => s.stepKind !== 'PRESTEP' && !!s.id);
  }

  get isDerivedWorkspace(): boolean {
    return this.workspaceMode === 'derived';
  }

  get isGoldenWorkspace(): boolean {
    return this.workspaceMode === 'golden';
  }

  get workspaceHeading(): string {
    return this.isDerivedWorkspace ? 'Derived recipe customization' : 'Golden recipe workspace';
  }

  get workspaceDescription(): string {
    return this.isDerivedWorkspace
      ? 'Customize the inherited recipe and keep an eye on the compatible machine list in the right drawer.'
      : 'Manage the golden reference recipe structure and parameter baseline from this workspace.';
  }

  get canDeleteStepFromContext(): boolean {
    return this.isGoldenWorkspace && Boolean(this.stepContextMenu.stepId);
  }

  get canDeleteRecipeWideParameterFromContext(): boolean {
    return this.isGoldenWorkspace && Boolean(this.parameterRowContextMenu.definitionId);
  }

  get canToggleGoldenFromContext(): boolean {
    const cell = this.cellActionBar.cell;
    const endpointCell = this.cellActionBar.endpointCell;
    if (!this.isGoldenWorkspace) {
      return false;
    }

    if (endpointCell) {
      return Boolean(endpointCell.stepId);
    }

    return !!cell?.stepParameterId && !cell.computed;
  }

  get goldenToggleLabel(): string {
    if (this.cellActionBar.endpointCell) {
      return this.cellActionBar.endpointCell.lockedByGolden ? 'Unlock golden' : 'Lock golden';
    }

    return this.cellActionBar.cell?.lockedByGolden ? 'Unlock golden' : 'Lock golden';
  }

  get canEditEndpointFromContext(): boolean {
    return Boolean(this.cellActionBar.endpointCell?.stepId);
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
        this.requestCompatibilityRefresh();
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
        this.requestCompatibilityRefresh();
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
        this.requestCompatibilityRefresh();
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
        this.requestCompatibilityRefresh();
      },
      error: (error) => console.error('Failed to remove required configuration', error)
    });
  }

  closeStepModal(): void {
    this.showStepModal = false;
  }

  openStepParameterModal(): void {
    if (!this.isGoldenWorkspace) {
      return;
    }

    const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
    const anchorStep =
      (selectedStep && selectedStep.stepKind !== 'PRESTEP' ? selectedStep : null)
      ?? this.recipeBuilderService.stepsSnapshot.find((s) => s.stepKind !== 'PRESTEP')
      ?? null;

    if (!anchorStep) {
      return;
    }

    this.prepareStepParameterModal(anchorStep);
  }

  openPrestepParameterModal(): void {
    if (!this.isGoldenWorkspace || !this.prestepStep) {
      return;
    }

    this.prepareStepParameterModal(this.prestepStep);
  }

  closeStepParameterModal(): void {
    this.showStepParameterModal = false;
    this.modalStepId = null;
    this.modalStepKind = null;
    this.modalParentCandidates = [];
    this.modalExcludedDefinitionIds = [];
  }

  closeEndpointModal(): void {
    this.showEndpointModal = false;
    this.endpointModalStepId = null;
    this.endpointModalLockedByGolden = false;
  }

  onEndpointSaved(): void {
    this.runWithViewportPreserved(() => {
      this.recipeBuilderService.refreshMatrix();
    });
    this.requestCompatibilityRefresh();
  }

  closeStepContextMenu(): void {
    this.stepContextMenu = {
      visible: false,
      x: 0,
      y: 0,
      stepId: null,
      stepCode: null
    };
  }

  closeCellActionBar(): void {
    this.cellActionBar = {
      visible: false,
      x: 0,
      y: 0,
      cell: null,
      endpointCell: null
    };
  }

  closeParameterRowContextMenu(): void {
    this.parameterRowContextMenu = {
      visible: false,
      x: 0,
      y: 0,
      definitionId: null,
      parameterName: null
    };
  }

  closeAllContextActions(): void {
    this.closeStepContextMenu();
    this.closeCellActionBar();
    this.closeParameterRowContextMenu();
  }

  ngOnDestroy(): void {
    this.clearCompatibilityFeedbackTimeout();
    this.destroy$.next();
    this.destroy$.complete();
    this.sub.unsubscribe();
    this.recipeBuilderService.reset();
  }

  
  private requestCompatibilityRefresh(trigger: 'sp-change' | 'other' = 'other'): void {
    const recipe = this.recipe$.value;
    if (!recipe?.id) return;

    // Pour une dérivée : recipeId = dérivée (pour les configs),
    //                    capabilitySourceRecipeId = golden parente (pour les capabilities)
    // Pour une golden  : recipeId = golden, pas de capabilitySourceRecipeId
    this.refreshCompatibilityForRecipe(recipe, trigger);
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
      .filter((row) => row.cells.some((cell) => Boolean(cell.stepParameterId)));

    return {
      ...matrix,
      columns: stepColumns,
      rows: filteredRows,
      endpointRow: (matrix.endpointRow ?? []).filter((cell) => stepColumnIds.has(cell.stepId))
    };
  }

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
      rows: filteredRows,
      endpointRow: (matrix.endpointRow ?? []).filter((cell) => cell.stepId === prestepColumn.stepId)
    };
  }

  private openEndpointModalForCell(endpointCell: RecipeMatrixEndpointCell): void {
    if (!endpointCell.stepId) {
      return;
    }

    this.endpointModalStepId = endpointCell.stepId;
    this.endpointModalLockedByGolden = endpointCell.lockedByGolden;
    this.showEndpointModal = true;
  }

  private prepareStepParameterModal(step: Step): void {
    if (!step.id) {
      return;
    }

    this.modalStepId = step.id;
    this.modalStepKind = step.stepKind;
    this.modalParentCandidates = [];
    this.modalExcludedDefinitionIds = this.collectInstantiatedDefinitionIds(step.stepKind, step.id);
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

  private latestRecipeMatrix: RecipeMatrix | null = null;

  private collectInstantiatedDefinitionIds(stepKind: StepKind, anchorStepId?: number): number[] {
    const matrix = this.latestRecipeMatrix;
    if (!matrix) {
      return [];
    }

    let scopedStepIds: Set<number>;

    if (stepKind === 'PRESTEP' && anchorStepId) {
      scopedStepIds = new Set([anchorStepId]);
    } else {
      scopedStepIds = new Set(
        matrix.columns
          .filter((column) => column.stepKind === stepKind)
          .map((column) => column.stepId)
      );

      if (scopedStepIds.size === 0) {
        scopedStepIds = new Set(
          this.recipeBuilderService.stepsSnapshot
            .filter((step) => step.stepKind === stepKind && Boolean(step.id))
            .map((step) => step.id as number)
        );
      }
    }

    if (scopedStepIds.size === 0) {
      return [];
    }

    const usedDefinitionIds = new Set<number>();
    for (const row of matrix.rows) {
      const hasInstantiatedCell = row.cells.some(
        (cell) => scopedStepIds.has(cell.stepId) && Boolean(cell.stepParameterId)
      );

      if (hasInstantiatedCell) {
        usedDefinitionIds.add(row.definitionId);
      }
    }

    return Array.from(usedDefinitionIds);
  }

  private openParameterDetailsModal(definitionId: number, fallbackName: string | null): void {
    this.showParameterDetailsModal = true;
    this.parameterDetailsLoading = true;
    this.parameterDetailsName = fallbackName ?? `Parameter #${definitionId}`;
    this.parameterDetailsCode = null;
    this.parameterDetailsAlias = null;
    this.parameterDetailsDescription = null;

    this.parameterDefinitionApiService.getDefinition(definitionId).subscribe({
      next: (definition) => {
        this.parameterDetailsName = definition.name || this.parameterDetailsName;
        this.parameterDetailsCode = definition.code ?? null;
        this.parameterDetailsAlias = definition.alias ?? null;
        this.parameterDetailsDescription = definition.description ?? null;
        this.parameterDetailsLoading = false;
      },
      error: (error) => {
        console.error('Failed to load parameter definition details', error);
        this.parameterDetailsDescription = 'Description unavailable for this parameter definition.';
        this.parameterDetailsLoading = false;
      }
    });
  }

  private loadRequirementsForWorkspace(recipe: Recipe): void {
    const parentRequirementsId = this.isDerivedWorkspace ? (recipe.parentRecipeId ?? null) : null;
    const primaryRequirementsId = parentRequirementsId ?? recipe.id ?? null;

    if (!primaryRequirementsId) {
      this.requirements$.next(this.buildEmptyRequirements(recipe));
      this.requestCompatibilityRefresh();
      return;
    }

    this.recipeRequirementsApiService.getRequirements(primaryRequirementsId).subscribe({
      next: (requirements) => {
        this.requirements$.next(requirements);
        this.requestCompatibilityRefresh();
      },
      error: (primaryError) => {
        if (this.isDerivedWorkspace && recipe.id && primaryRequirementsId !== recipe.id) {
          this.recipeRequirementsApiService.getRequirements(recipe.id).subscribe({
            next: (requirements) => {
              this.requirements$.next(requirements);
              this.requestCompatibilityRefresh();
            },
            error: (fallbackError) => {
              console.error('Failed to load recipe requirements (parent and derived fallback)', {
                primaryError,
                fallbackError
              });
              this.requirements$.next(this.buildEmptyRequirements(recipe, primaryRequirementsId));
              this.requestCompatibilityRefresh();
            }
          });
          return;
        }

        console.error('Failed to load recipe requirements', primaryError);
        this.requirements$.next(this.buildEmptyRequirements(recipe, primaryRequirementsId));
        this.requestCompatibilityRefresh();
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

  private updateRecomputedPulseFromMatrixDelta(previous: RecipeMatrix | null, current: RecipeMatrix | null): void {
    if (!previous || !current) {
      this.recomputedPulseStepParameterIds = [];
      return;
    }

    const previousById = this.buildCellByStepParameterIdMap(previous);
    const pulseIds: number[] = [];

    for (const row of current.rows ?? []) {
      for (const cell of row.cells ?? []) {
        if (!cell.computed || !cell.computedFromModified || cell.stepParameterId == null) {
          continue;
        }

        const previousCell = previousById.get(cell.stepParameterId);
        if (!previousCell || this.hasCellDisplayChanged(previousCell, cell)) {
          pulseIds.push(cell.stepParameterId);
        }
      }
    }

    this.recomputedPulseStepParameterIds = pulseIds;
    if (pulseIds.length > 0) {
      this.recomputedPulseTick += 1;
    }
  }

  private buildCellByStepParameterIdMap(matrix: RecipeMatrix): Map<number, RecipeMatrixCell> {
    const byId = new Map<number, RecipeMatrixCell>();

    for (const row of matrix.rows ?? []) {
      for (const cell of row.cells ?? []) {
        if (cell.stepParameterId != null) {
          byId.set(cell.stepParameterId, cell);
        }
      }
    }

    return byId;
  }

  private hasCellDisplayChanged(previousCell: RecipeMatrixCell, currentCell: RecipeMatrixCell): boolean {
    return previousCell.displayValue !== currentCell.displayValue
      || previousCell.valueJson !== currentCell.valueJson
      || previousCell.selectedOptionId !== currentCell.selectedOptionId
      || previousCell.computedFromModified !== currentCell.computedFromModified;
  }

  refreshCompatibilityByCapabilitiesOnly(): void {
    this.requestCompatibilityRefresh();
  }

  refreshCompatibility(recipeId: number | null): void {
    // Conservé pour le RecipeCompatibilityRefreshService (appelé depuis d'autres endroits)
    // Mais maintenant on passe par refreshCompatibilityForRecipe si on a la recette complète
    const recipe = this.recipe$.value;
    if (recipe?.id === recipeId) {
      this.refreshCompatibilityForRecipe(recipe, 'other');
      return;
    }

    // Fallback : pas de recette en mémoire, on ne peut pas savoir le parentRecipeId
    if (recipeId == null || Number.isNaN(recipeId)) return;

    this.compatibilityLoading = true;
    const request: RecipeCompatibleMachineSearchRequest = {
      recipeId,
      capabilitySourceRecipeId: null,  // on n'a pas l'info → capabilities depuis la recette elle-même
      configurationConstraints: null   // calcul depuis les StepParameters
    };

    this.executeCompatibilityRequest(request, 'other');
  }
  // Nouvelle méthode centrale
  private refreshCompatibilityForRecipe(recipe: Recipe, trigger: 'sp-change' | 'other' = 'other'): void {
    if (!recipe?.id) return;

    this.compatibilityLoading = true;

    const isDerived = recipe.recipeKind === 'DERIVED';

    const request: RecipeCompatibleMachineSearchRequest = {
      recipeId: recipe.id,
      // Si dérivée : capabilities lues depuis la golden parente
      capabilitySourceRecipeId: isDerived ? (recipe.parentRecipeId ?? null) : null,
      // null → calcul automatique depuis les StepParameters de recipeId (la dérivée)
      // C'est exactement ce qu'on veut : les valeurs que l'user est en train de modifier
      configurationConstraints: null
    };

    this.executeCompatibilityRequest(request, trigger);
  }

  // Extraction de la logique HTTP pour éviter la duplication
  private executeCompatibilityRequest(
    request: RecipeCompatibleMachineSearchRequest,
    trigger: 'sp-change' | 'other' = 'other'
  ): void {
    const feedbackToken = trigger === 'sp-change'
      ? this.beginCompatibilityFilteringFeedback()
      : null;

    this.recipeCompatibilityApiService.findCompatibleMachines(request)
      .pipe(finalize(() => {
        this.compatibilityLoading = false;
        if (feedbackToken != null) {
          this.completeCompatibilityFilteringFeedback(feedbackToken);
        }
      }))
      .subscribe({
        next: result => {
          this.compatibility = result;
          this.compatibility$.next(result);
        },
        error: (error) => {
          console.error('Failed to refresh compatibility', error);
          const empty = this.buildEmptyCompatibility(request.recipeId as number);
          this.compatibility = empty;
          this.compatibility$.next(empty);
        }
      });
  }

  private beginCompatibilityFilteringFeedback(): number {
    this.compatibilityFeedbackToken += 1;
    const token = this.compatibilityFeedbackToken;

    this.clearCompatibilityFeedbackTimeout();
    this.compatibilityFilteringFeedbackActive = true;
    this.compatibilityFilteringFeedbackTick += 1;

    return token;
  }

  private completeCompatibilityFilteringFeedback(token: number): void {
    if (token !== this.compatibilityFeedbackToken) {
      return;
    }

    this.clearCompatibilityFeedbackTimeout();
    this.compatibilityFeedbackTimeoutId = setTimeout(() => {
      if (token === this.compatibilityFeedbackToken) {
        this.compatibilityFilteringFeedbackActive = false;
      }
      this.compatibilityFeedbackTimeoutId = null;
    }, 1000);
  }

  private clearCompatibilityFeedbackTimeout(): void {
    if (this.compatibilityFeedbackTimeoutId == null) {
      return;
    }

    clearTimeout(this.compatibilityFeedbackTimeoutId);
    this.compatibilityFeedbackTimeoutId = null;
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

  private buildStepParameterCreatePayload(source: StepParameter): StepParameter {
    const payload = { ...(source as any) };
    delete payload.id;
    delete payload.step;
    delete payload.stepId;
    delete payload.createdAt;
    delete payload.updatedAt;
    payload.parentStepParameterId = null;
    return payload as StepParameter;
  }

  private runWithViewportPreserved(action: () => void): void {
    if (typeof window === 'undefined' || typeof document === 'undefined') {
      action();
      return;
    }

    const selectors = [
      '.workspace__main',
      '.steps-shell__content',
      '.matrix-wrapper'
    ];

    const capture = (selector: string) => {
      const element = document.querySelector(selector) as HTMLElement | null;
      return {
        selector,
        top: element?.scrollTop ?? 0,
        left: element?.scrollLeft ?? 0
      };
    };

    const windowPosition = {
      x: window.scrollX,
      y: window.scrollY
    };

    const scrollStates = selectors.map(capture);

    action();

    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        window.scrollTo(windowPosition.x, windowPosition.y);

        for (const state of scrollStates) {
          const element = document.querySelector(state.selector) as HTMLElement | null;
          if (element) {
            element.scrollTop = state.top;
            element.scrollLeft = state.left;
          }
        }
      });
    });
  }

  private openXmlExportReviewModal(): void {
    const recipe = this.recipe$.value;
    if (recipe?.id) {
      this.loadXmlExportCustomizationSummary(recipe.id);
    }

    const { machineId, chamberId } = this.resolveDefaultXmlExportTarget();

    this.xmlExportSelectedMachineId = machineId;
    this.xmlExportSelectedChamberId = chamberId;
    this.xmlExportSelectedChamberDetail = null;
    this.xmlExportUnderstandChecked = false;
    this.showXmlExportReviewModal = true;

    if (chamberId != null) {
      this.onXmlExportChamberChanged(chamberId);
    }
  }

  private loadXmlExportCustomizationSummary(recipeId: number): void {
    this.xmlExportLoadingCustomizationSummary = true;

    // TODO(product): tune the list limit according to UX expectations in the modal.
    this.recipeApiService.getCustomizationSummary(recipeId, 150).subscribe({
      next: (summary) => {
        this.xmlExportCustomizationSummary = summary;
        this.xmlExportLoadingCustomizationSummary = false;
      },
      error: (error) => {
        console.error('Failed to load customization summary for XML export review', error);
        this.xmlExportCustomizationSummary = null;
        this.xmlExportLoadingCustomizationSummary = false;
      }
    });
  }

  private resolveDefaultXmlExportTarget(): { machineId: number | null; chamberId: number | null } {
    const firstMachine = this.compatibility?.machines?.[0] ?? null;
    if (!firstMachine) {
      return { machineId: null, chamberId: null };
    }

    const firstChamber = firstMachine.compatibleChambers?.[0] ?? null;
    return {
      machineId: firstMachine.machineId,
      chamberId: firstChamber?.chamberId ?? null
    };
  }

  private downloadXmlForCurrentSelectionOrDefault(): void {
    const recipe = this.recipe$.value;
    const recipeId = recipe?.id;
    if (recipeId == null) {
      return;
    }

    const machineId = this.xmlExportSelectedMachineId ?? this.resolveDefaultXmlExportTarget().machineId;
    if (this.isDerivedWorkspace && machineId == null) {
      window.alert('No target machine available for XML export.');
      return;
    }

    this.xmlExportDownloading = true;

    this.recipeApiService.exportRecipeXml(recipeId, machineId)
      .pipe(finalize(() => {
        this.xmlExportDownloading = false;
      }))
      .subscribe({
        next: (response) => {
          const filename = this.resolveXmlFilename(response.headers.get('content-disposition'), recipeId, machineId);
          this.triggerBrowserDownload(response.body, filename);

          if (this.showXmlExportReviewModal) {
            this.closeXmlExportReviewModal();
          }
        },
        error: (error) => {
          if (error instanceof HttpErrorResponse && error.status === 409) {
            const serverMessage = typeof error.error === 'string' && error.error.trim().length > 0
              ? error.error
              : (error.message || 'XML export blocked by strict customization policy.');
            window.alert(serverMessage);
            return;
          }

          console.error('Failed to export recipe XML', error);
        }
      });
  }

  private resolveXmlFilename(contentDisposition: string | null, recipeId: number, machineId?: number | null): string {
    if (contentDisposition) {
      const match = contentDisposition.match(/filename\*?=(?:UTF-8''|\")?([^\";]+)/i);
      if (match?.[1]) {
        return decodeURIComponent(match[1].trim().replace(/^"|"$/g, ''));
      }
    }

    if (machineId != null) {
      return `recipe_${recipeId}_machine_${machineId}.xml`;
    }

    return `recipe_${recipeId}.xml`;
  }

  private triggerBrowserDownload(payload: Blob | null, filename: string): void {
    if (!payload) {
      return;
    }

    const blobUrl = window.URL.createObjectURL(payload);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(blobUrl);
  }
}