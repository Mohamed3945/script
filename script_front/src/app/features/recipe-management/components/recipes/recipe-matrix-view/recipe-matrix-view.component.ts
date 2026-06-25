import { NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParameterValueType } from '../../../../../core/models/parameter-value-type.model';
import { RecipeMatrix } from '../../../../../core/models/recipe-matrix.model';
import { RecipeMatrixCell } from '../../../../../core/models/recipe-matrix-cell.model';

export interface RecipeMatrixCellUpdate {
  cell: RecipeMatrixCell;
  valueType: ParameterValueType;
  selectedOptionId?: number | null;
  valueJson?: string | null;
}

@Component({
  selector: 'app-recipe-matrix-view',
  standalone: true,
  imports: [NgIf, NgFor, NgSwitch, NgSwitchCase, NgSwitchDefault, FormsModule],
  templateUrl: './recipe-matrix-view.component.html',
  styleUrl: './recipe-matrix-view.component.scss'
})
export class RecipeMatrixViewComponent {
  @Input() matrix: RecipeMatrix | null = null;

  @Output() addStepClicked = new EventEmitter<void>();
  @Output() cellUpdated = new EventEmitter<RecipeMatrixCellUpdate>();

  readonly enumValueType: ParameterValueType = 'ENUM';
  readonly numberValueType: ParameterValueType = 'NUMBER';

  get stepColumns() {
    return this.matrix?.columns ?? [];
  }

  get parameterRows() {
    return this.matrix?.rows ?? [];
  }

  isEditable(cell: RecipeMatrixCell): boolean {
    return Boolean(cell.editable) && !cell.lockedByGolden && Boolean(cell.stepParameterId);
  }

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

  onValueCommitted(cell: RecipeMatrixCell, rawValue: string): void {
    if (!this.isEditable(cell)) {
      return;
    }

    this.cellUpdated.emit({
      cell,
      valueType: cell.valueType,
      valueJson: rawValue ?? null,
      selectedOptionId: null
    });
  }

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
