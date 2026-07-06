import { Component, Input } from '@angular/core';
import { StepParameterGridRow } from '../../../../../core/models/step-parameter-grid-row.model';
import { StepParameterGridComponent } from '../../steps/step-parameter-grid/step-parameter-grid.component';

@Component({
  selector: 'app-recipe-step-grid-panel',
  standalone: true,
  imports: [StepParameterGridComponent],
  templateUrl: './recipe-step-grid-panel.component.html',
  styleUrl: './recipe-step-grid-panel.component.scss'
})
/**
 * RecipeStepGridPanelComponent coordinates UI logic for this feature.
 */
export class RecipeStepGridPanelComponent {
  @Input() rows: StepParameterGridRow[] = [];
}
