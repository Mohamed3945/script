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
}
