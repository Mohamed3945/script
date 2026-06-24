import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Subscription } from 'rxjs';
import { Recipe } from '../../../../core/models/recipe.model';
import { Step } from '../../../../core/models/step.model';
import { StepParameter } from '../../../../core/models/step-parameter.model';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { RecipeBuilderService } from '../../../../core/services/recipe-builder.service';
import { RecipeDetailHeaderComponent } from '../../components/recipes/recipe-detail-header/recipe-detail-header.component';
import { RecipeSummarySidecardComponent } from '../../components/recipes/recipe-summary-sidecard/recipe-summary-sidecard.component';
import { RecipeTabNavComponent, RecipeWorkspaceTab } from '../../components/recipes/recipe-tab-nav/recipe-tab-nav.component';
import { RecipeViewSwitchComponent, RecipeViewMode } from '../../components/recipes/recipe-view-switch/recipe-view-switch.component';
import { RecipeMatrixViewComponent } from '../../components/recipes/recipe-matrix-view/recipe-matrix-view.component';
import { RecipeStepFocusViewComponent } from '../../components/recipes/recipe-step-focus-view/recipe-step-focus-view.component';
import { StepModalComponent } from '../../components/steps/step-modal/step-modal.component';
import { StepParameterModalComponent } from '../../components/steps/step-parameter-modal/step-parameter-modal.component';

@Component({
  selector: 'app-recipe-detail-page',
  standalone: true,
  imports: [
    NgIf,
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
  gridRows$;
  loading$;

  activeTab: RecipeWorkspaceTab = 'steps';
  viewMode: RecipeViewMode = 'matrix';

  showStepModal = false;
  showStepParameterModal = false;

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
    this.gridRows$ = this.recipeBuilderService.gridRows$;
    this.loading$ = this.recipeBuilderService.loading$;
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
    const selectedStep = this.recipeBuilderService.selectedStepSnapshot;
    if (!selectedStep?.id) return;

    this.recipeApiService.createStepParameter(selectedStep.id, payload).subscribe({
      next: () => {
        this.showStepParameterModal = false;
        this.recipeBuilderService.loadStepParameters(selectedStep.id!);
        this.recipeBuilderService.refreshGrid();
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
        this.recipeBuilderService.refreshGrid();
      },
      error: (error) => console.error('Failed to delete step parameter', error)
    });
  }

  openStepModal(): void {
    this.showStepModal = true;
  }

  closeStepModal(): void {
    this.showStepModal = false;
  }

  openStepParameterModal(): void {
    this.showStepParameterModal = true;
  }

  closeStepParameterModal(): void {
    this.showStepParameterModal = false;
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    this.recipeBuilderService.reset();
  }
}