import { Component, Input } from '@angular/core';
import { Recipe } from '../../../../../core/models/recipe.model';

@Component({
  selector: 'app-recipe-summary-sidecard',
  standalone: true,
  templateUrl: './recipe-summary-sidecard.component.html',
  styleUrl: './recipe-summary-sidecard.component.scss'
})
export class RecipeSummarySidecardComponent {
  @Input() recipe!: Recipe;
}
