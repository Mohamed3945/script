import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';
import { Recipe } from '../../../../../core/models/recipe.model';
import { DerivedRecipeBannerComponent } from '../derived-recipe-banner/derived-recipe-banner.component';

@Component({
  selector: 'app-recipe-summary-card',
  standalone: true,
  imports: [NgIf, DerivedRecipeBannerComponent],
  templateUrl: './recipe-summary-card.component.html',
  styleUrl: './recipe-summary-card.component.scss'
})
/**
 * RecipeSummaryCardComponent coordinates UI logic for this feature.
 */
export class RecipeSummaryCardComponent {
  @Input() recipe!: Recipe;
}
