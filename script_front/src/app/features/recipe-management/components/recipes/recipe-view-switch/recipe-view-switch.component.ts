import { Component, EventEmitter, Input, Output } from '@angular/core';

export type RecipeViewMode = 'matrix' | 'focus';

@Component({
  selector: 'app-recipe-view-switch',
  standalone: true,
  templateUrl: './recipe-view-switch.component.html',
  styleUrl: './recipe-view-switch.component.scss'
})
/**
 * RecipeViewSwitchComponent coordinates UI logic for this feature.
 */
export class RecipeViewSwitchComponent {
  @Input() mode: RecipeViewMode = 'matrix';
  @Output() modeChanged = new EventEmitter<RecipeViewMode>();
}
