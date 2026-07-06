import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { buildRecipeDetailRouteByKind } from '../../../../core/utils/recipe-route.util';
import { RecipeFormComponent } from '../../components/recipes/recipe-form/recipe-form.component';

@Component({
  selector: 'app-recipe-derived-create-page',
  standalone: true,
  imports: [RecipeFormComponent],
  templateUrl: './recipe-derived-create-page.component.html',
  styleUrl: './recipe-derived-create-page.component.scss'
})
/**
 * RecipeDerivedCreatePageComponent coordinates UI logic for this feature.
 */
export class RecipeDerivedCreatePageComponent implements OnInit {
  initialRecipe: Recipe = {
    recipeKind: 'DERIVED',
    parentRecipeId: null,
    name: '',
    description: '',
    creatorId: 0,
    revisorId: null,
    processFamily: '',
    status: 'DRAFT',
    version: null,
    frozen: false
  };

  resultProfileId?: number;

  constructor(
    private route: ActivatedRoute,
    private recipeApiService: RecipeApiService,
    private router: Router
  ) {}

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    const resultProfileId = this.route.snapshot.queryParamMap.get('resultProfileId');
    const parentRecipeId = this.route.snapshot.queryParamMap.get('parentRecipeId');

    if (resultProfileId) {
      this.resultProfileId = Number(resultProfileId);
    }

    if (parentRecipeId) {
      this.initialRecipe.parentRecipeId = Number(parentRecipeId);
    }
  }

  onSubmit(payload: { recipe: Recipe; resultProfileId?: number }): void {
    this.recipeApiService.createRecipe(
      { ...payload.recipe, recipeKind: 'DERIVED' },
      payload.resultProfileId ?? this.resultProfileId
    ).subscribe({
      next: (recipe) => {
        if (recipe.id) {
          this.router.navigate(buildRecipeDetailRouteByKind(recipe.id, 'DERIVED'));
        } else {
          this.router.navigate(['/recipes']);
        }
      },
      error: (error) => {
        console.error('Failed to create derived recipe', error);
      }
    });
  }
}
