import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RecipeKind } from '../../../../../core/models/recipe-kind.model';

@Component({
  selector: 'app-recipe-list-filters',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './recipe-list-filters.component.html',
  styleUrl: './recipe-list-filters.component.scss'
})
export class RecipeListFiltersComponent {
  @Output() filtersChanged = new EventEmitter<{ recipeKind: RecipeKind | ''; golden: boolean | undefined }>();

  recipeKind: RecipeKind | '' = '';
  goldenValue = '';

  applyFilters(): void {
    let golden: boolean | undefined = undefined;

    if (this.goldenValue === 'true') {
      golden = true;
    } else if (this.goldenValue === 'false') {
      golden = false;
    }

    this.filtersChanged.emit({
      recipeKind: this.recipeKind,
      golden
    });
  }

  resetFilters(): void {
    this.recipeKind = '';
    this.goldenValue = '';
    this.applyFilters();
  }
}