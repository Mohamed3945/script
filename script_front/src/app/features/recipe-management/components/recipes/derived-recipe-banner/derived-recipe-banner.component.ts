import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-derived-recipe-banner',
  standalone: true,
  imports: [NgIf],
  templateUrl: './derived-recipe-banner.component.html',
  styleUrl: './derived-recipe-banner.component.scss'
})
/**
 * DerivedRecipeBannerComponent coordinates UI logic for this feature.
 */
export class DerivedRecipeBannerComponent {
  @Input() parentRecipeId?: number | null;
}
