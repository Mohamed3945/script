import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Recipe } from '../../../../../core/models/recipe.model';

@Component({
  selector: 'app-recipe-list-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './recipe-list-table.component.html',
  styleUrl: './recipe-list-table.component.scss'
})
/**
 * RecipeListTableComponent coordinates UI logic for this feature.
 */
export class RecipeListTableComponent {
  @Input() recipes: Recipe[] = [];

  @Output() viewRecipe = new EventEmitter<Recipe>();
  @Output() editRecipe = new EventEmitter<Recipe>();
  @Output() deleteRecipe = new EventEmitter<Recipe>();

  readonly pageSize = 15;
  filterText = '';
  currentPage = 1;

  onFilterChange(value: string): void {
    this.filterText = value;
    this.currentPage = 1;
  }

  get filteredRecipes(): Recipe[] {
    const query = this.filterText.trim().toLowerCase();
    if (!query) {
      return this.recipes;
    }

    return this.recipes.filter((recipe) => JSON.stringify(recipe).toLowerCase().includes(query));
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredRecipes.length / this.pageSize));
  }

  get pagedRecipes(): Recipe[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredRecipes.slice(start, start + this.pageSize);
  }

  previousPage(): void {
    this.currentPage = Math.max(1, this.currentPage - 1);
  }

  nextPage(): void {
    this.currentPage = Math.min(this.totalPages, this.currentPage + 1);
  }
}
