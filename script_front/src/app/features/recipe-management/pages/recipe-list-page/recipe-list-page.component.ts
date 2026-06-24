import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeKind } from '../../../../core/models/recipe-kind.model';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { RecipeListTableComponent } from '../../components/recipes/recipe-list-table/recipe-list-table.component';
import { RecipeListFiltersComponent } from '../../components/recipes/recipe-list-filters/recipe-list-filters.component';

@Component({
  selector: 'app-recipe-list-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, RecipeListTableComponent, RecipeListFiltersComponent],
  templateUrl: './recipe-list-page.component.html',
  styleUrl: './recipe-list-page.component.scss'
})
export class RecipeListPageComponent implements OnInit {
  recipes$ = new BehaviorSubject<Recipe[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);

  currentKind: RecipeKind | '' = '';
  currentGolden?: boolean;

  constructor(
    private recipeApiService: RecipeApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRecipes();
  }

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

  onCreateGolden(): void {
    this.router.navigate(['/recipes/new']);
  }

  onCreateDerived(): void {
    this.router.navigate(['/recipes/new-derived']);
  }

  onViewRecipe(recipe: Recipe): void {
    if (!recipe.id) return;
    this.router.navigate(['/recipes', recipe.id]);
  }

  onEditRecipe(recipe: Recipe): void {
    if (!recipe.id) return;
    this.router.navigate(['/recipes', recipe.id, 'edit']);
  }

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