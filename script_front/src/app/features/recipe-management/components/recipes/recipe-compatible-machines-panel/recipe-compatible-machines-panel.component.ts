import { Component, Input } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RecipeCompatibilityResult } from '../../../../../core/models/recipe-compatibility-result.model';

@Component({
  selector: 'app-recipe-compatible-machines-panel',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './recipe-compatible-machines-panel.component.html',
  styleUrl: './recipe-compatible-machines-panel.component.scss'
})
export class RecipeCompatibleMachinesPanelComponent {
  @Input() compatibility: RecipeCompatibilityResult | null = null;
}
