import { NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Step } from '../../../../../core/models/step.model';
import { RecipeMatrixCell } from '../../../../../core/models/recipe-matrix-cell.model';
import { ParameterValueType } from '../../../../../core/models/parameter-value-type.model';

export interface PrestepRow {
  parameterName: string;
  parameterAlias: string;
  valueType: ParameterValueType;
  cell: RecipeMatrixCell;
}

export interface PrestepCellUpdate {
  cell: RecipeMatrixCell;
  valueType: ParameterValueType;
  selectedOptionId?: number | null;
  valueJson?: string | null;
}

@Component({
  selector: 'app-recipe-prestep-view',
  standalone: true,
  imports: [NgIf, NgFor, NgSwitch, NgSwitchCase, NgSwitchDefault, FormsModule],
  templateUrl: './recipe-prestep-view.component.html',
  styleUrl: './recipe-prestep-view.component.scss'
})
/**
 * RecipePrestepViewComponent coordinates UI logic for this feature.
 */
export class RecipePrestepViewComponent {
  @Input() prestepStep: Step | null = null;
  @Input() rows: PrestepRow[] | null = [];

  @Output() addParameterClicked = new EventEmitter<void>();
  @Output() deleteParameterClicked = new EventEmitter<RecipeMatrixCell>();
  @Output() cellUpdated = new EventEmitter<PrestepCellUpdate>();

  readonly enumValueType: ParameterValueType = 'ENUM';
  readonly numberValueType: ParameterValueType = 'NUMBER';

  /**
   * Handles the isEditable workflow.
   */
  isEditable(cell: RecipeMatrixCell): boolean {
    const isActive = !cell.activationState || cell.activationState === 'ENABLED';
    return Boolean(cell.editable) && !cell.lockedByGolden && Boolean(cell.stepParameterId) && isActive;
  }

  /**
   * Handles the onEnumChanged workflow.
   */
  onEnumChanged(cell: RecipeMatrixCell, selectedValue: string): void {
    if (!this.isEditable(cell)) {
      return;
    }

    const selectedOptionId = selectedValue ? Number(selectedValue) : null;
    this.cellUpdated.emit({
      cell,
      valueType: 'ENUM',
      selectedOptionId,
      valueJson: null
    });
  }

  /**
   * Handles the onValueCommitted workflow.
   */
  onValueCommitted(cell: RecipeMatrixCell, rawValue: string): void {
    if (!this.isEditable(cell)) {
      return;
    }

    this.cellUpdated.emit({
      cell,
      valueType: cell.valueType,
      selectedOptionId: null,
      valueJson: rawValue ?? null
    });
  }

  /**
   * Handles the onNumberCommitted workflow.
   */
  onNumberCommitted(cell: RecipeMatrixCell, rawValue: string): void {
    if (!this.isEditable(cell)) {
      return;
    }

    const trimmed = (rawValue ?? '').trim();
    if (trimmed.length === 0) {
      this.onValueCommitted(cell, '');
      return;
    }

    const asNumber = Number(trimmed);
    if (Number.isNaN(asNumber)) {
      return;
    }

    this.onValueCommitted(cell, String(asNumber));
  }
}
