import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { RecipeFormComponent } from '../../components/recipes/recipe-form/recipe-form.component';

@Component({
  selector: 'app-recipe-create-page',
  standalone: true,
  imports: [RecipeFormComponent],
  templateUrl: './recipe-create-page.component.html',
  styleUrl: './recipe-create-page.component.scss'
})
export class RecipeCreatePageComponent {
  constructor(
    private recipeApiService: RecipeApiService,
    private router: Router
  ) {}

  onSubmit(payload: { recipe: Recipe; resultProfileId?: number }): void {
    this.recipeApiService.createRecipe(payload.recipe, payload.resultProfileId).subscribe({
      next: (recipe) => {
        if (recipe.id) {
          this.router.navigate(['/recipes', recipe.id]);
        } else {
          this.router.navigate(['/recipes']);
        }
      },
      error: (error) => {
        console.error('Failed to create recipe', error);
      }
    });
  }
}