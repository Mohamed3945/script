import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Recipe } from '../../../../../core/models/recipe.model';

@Component({
  selector: 'app-recipe-detail-header',
  standalone: true,
  templateUrl: './recipe-detail-header.component.html',
  styleUrl: './recipe-detail-header.component.scss'
})
/**
 * RecipeDetailHeaderComponent coordinates UI logic for this feature.
 */
export class RecipeDetailHeaderComponent {
  @Input() recipe!: Recipe;

  @Output() editClicked = new EventEmitter<void>();
  @Output() deleteClicked = new EventEmitter<void>();
}
