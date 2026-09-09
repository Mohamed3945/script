import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeKind } from '../../../../core/models/recipe-kind.model';
import { DuplicateGoldenRecipePayload, RecipeApiService } from '../../../../core/services/recipe-api.service';
import { buildRecipeDetailRouteByKind } from '../../../../core/utils/recipe-route.util';
import { RecipeListTableComponent } from '../../components/recipes/recipe-list-table/recipe-list-table.component';
import { RecipeListFiltersComponent } from '../../components/recipes/recipe-list-filters/recipe-list-filters.component';

@Component({
  selector: 'app-recipe-list-page',
  standalone: true,
  imports: [NgIf, NgFor, AsyncPipe, FormsModule, RecipeListTableComponent, RecipeListFiltersComponent],
  templateUrl: './recipe-list-page.component.html',
  styleUrl: './recipe-list-page.component.scss'
})
/**
 * RecipeListPageComponent coordinates UI logic for this feature.
 */
export class RecipeListPageComponent implements OnInit {
  recipes$ = new BehaviorSubject<Recipe[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  duplicateLoading$ = new BehaviorSubject<boolean>(false);

  currentKind: RecipeKind | '' = '';
  currentGolden?: boolean;

  showCreateGoldenModal = false;
  createMode: 'SCRATCH' | 'EXISTING' = 'SCRATCH';
  availableGoldenRecipes: Recipe[] = [];
  selectedSourceGoldenId: number | null = null;
  targetGoldenName = '';
  includeFormulas = false;
  includeRequiredCapabilities = false;
  includeRequiredConfigurations = false;

  constructor(
    private recipeApiService: RecipeApiService,
    private router: Router
  ) {}

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    this.loadRecipes();
  }

  /**
   * Handles the loadRecipes workflow.
   */
  loadRecipes(): void {
    this.loading$.next(true);

    this.recipeApiService.getRecipes({
      recipeKind: this.currentKind || undefined,
      golden: this.currentGolden
    }).subscribe({
      next: (recipes) => {
        this.recipes$.next(recipes);
        this.loading$.next(false);
      },
      error: (error) => {
        console.error('Failed to load recipes', error);
        this.recipes$.next([]);
        this.loading$.next(false);
      }
    });
  }

  onFiltersChanged(filters: { recipeKind: RecipeKind | ''; golden: boolean | undefined }): void {
    this.currentKind = filters.recipeKind;
    this.currentGolden = filters.golden;
    this.loadRecipes();
  }

  /**
   * Handles the onCreateGolden workflow.
   */
  onCreateGolden(): void {
    this.openCreateGoldenModal();
  }

  openCreateGoldenModal(): void {
    this.showCreateGoldenModal = true;
    this.createMode = 'SCRATCH';
    this.selectedSourceGoldenId = null;
    this.targetGoldenName = '';
    this.includeFormulas = false;
    this.includeRequiredCapabilities = false;
    this.includeRequiredConfigurations = false;

    this.recipeApiService.getRecipes({ recipeKind: 'GOLDEN' }).subscribe({
      next: (recipes) => {
        this.availableGoldenRecipes = recipes;
      },
      error: (error) => {
        console.error('Failed to load golden recipes for duplication', error);
        this.availableGoldenRecipes = [];
      }
    });
  }

  closeCreateGoldenModal(): void {
    if (this.duplicateLoading$.value) {
      return;
    }
    this.showCreateGoldenModal = false;
  }

  confirmCreateGolden(): void {
    if (this.createMode === 'SCRATCH') {
      this.showCreateGoldenModal = false;
      this.router.navigate(['/recipes/new']);
      return;
    }

    if (!this.selectedSourceGoldenId || this.targetGoldenName.trim().length === 0) {
      return;
    }

    const payload: DuplicateGoldenRecipePayload = {
      sourceGoldenRecipeId: this.selectedSourceGoldenId,
      targetName: this.targetGoldenName.trim(),
      includeFormulas: this.includeFormulas,
      includeRequiredCapabilities: this.includeRequiredCapabilities,
      includeRequiredConfigurations: this.includeRequiredConfigurations
    };

    this.duplicateLoading$.next(true);
    this.recipeApiService.duplicateGoldenRecipe(payload).subscribe({
      next: (recipe) => {
        this.duplicateLoading$.next(false);
        this.showCreateGoldenModal = false;
        if (!recipe.id) {
          this.loadRecipes();
          return;
        }
        this.router.navigate(buildRecipeDetailRouteByKind(recipe.id, recipe.recipeKind));
      },
      error: (error) => {
        console.error('Failed to duplicate golden recipe', error);
        this.duplicateLoading$.next(false);
      }
    });
  }

  get canSubmitCreateGolden(): boolean {
    if (this.createMode === 'SCRATCH') {
      return true;
    }
    return this.selectedSourceGoldenId != null
      && this.targetGoldenName.trim().length > 0
      && !this.duplicateLoading$.value;
  }

  onOpenCapabilitiesCatalog(): void {
    this.router.navigate(['/reference-data/capabilities']);
  }

  onCreateCapability(): void {
    this.router.navigate(['/reference-data/capabilities/new']);
  }

  /**
   * Handles the onViewRecipe workflow.
   */
  onViewRecipe(recipe: Recipe): void {
    if (!recipe.id) return;
    this.router.navigate(buildRecipeDetailRouteByKind(recipe.id, recipe.recipeKind));
  }

  /**
   * Handles the onEditRecipe workflow.
   */
  onEditRecipe(recipe: Recipe): void {
    if (!recipe.id) return;
    this.router.navigate(['/recipes', recipe.id, 'edit']);
  }

  /**
   * Handles the onDeleteRecipe workflow.
   */
  onDeleteRecipe(recipe: Recipe): void {
    if (!recipe.id) return;

    const confirmed = window.confirm(`Delete recipe "${recipe.name}"?`);
    if (!confirmed) return;

    this.recipeApiService.deleteRecipe(recipe.id).subscribe({
      next: () => this.loadRecipes(),
      error: (error) => console.error('Failed to delete recipe', error)
    });
  }
}
