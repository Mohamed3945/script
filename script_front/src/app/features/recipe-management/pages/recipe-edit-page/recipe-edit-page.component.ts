import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { RecipeFormComponent } from '../../components/recipes/recipe-form/recipe-form.component';

@Component({
  selector: 'app-recipe-edit-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, RecipeFormComponent],
  templateUrl: './recipe-edit-page.component.html',
  styleUrl: './recipe-edit-page.component.scss'
})
export class RecipeEditPageComponent implements OnInit {
  recipe$ = new BehaviorSubject<Recipe | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeApiService: RecipeApiService
  ) {}

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

    this.recipeApiService.updateRecipe(current.id, payload.recipe).subscribe({
      next: () => this.router.navigate(['/recipes', current.id]),
      error: (error) => console.error('Failed to update recipe', error)
    });
  }
}

