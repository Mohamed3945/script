import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { buildRecipeDetailRouteByKind } from '../../../../core/utils/recipe-route.util';
import { RecipeFormComponent } from '../../components/recipes/recipe-form/recipe-form.component';

@Component({
  selector: 'app-recipe-edit-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, RecipeFormComponent],
  templateUrl: './recipe-edit-page.component.html',
  styleUrl: './recipe-edit-page.component.scss'
})
/**
 * RecipeEditPageComponent coordinates UI logic for this feature.
 */
export class RecipeEditPageComponent implements OnInit {
  recipe$ = new BehaviorSubject<Recipe | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeApiService: RecipeApiService
  ) {}

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.recipeApiService.getRecipeById(id).subscribe({
      next: (recipe) => this.recipe$.next(recipe),
      error: (error) => console.error('Failed to load recipe', error)
    });
  }

  onSubmit(payload: { recipe: Recipe; resultProfileId?: number }): void {
    const current = this.recipe$.value;
    if (!current?.id) return;
    const recipeId = current.id;

    this.recipeApiService.updateRecipe(recipeId, payload.recipe).subscribe({
      next: () => this.router.navigate(buildRecipeDetailRouteByKind(recipeId, current.recipeKind)),
      error: (error) => console.error('Failed to update recipe', error)
    });
  }
}

