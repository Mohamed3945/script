import { NgClass, NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParameterValueType } from '../../../../../core/models/parameter-value-type.model';
import { RecipeMatrix } from '../../../../../core/models/recipe-matrix.model';
import { RecipeMatrixCell } from '../../../../../core/models/recipe-matrix-cell.model';
import { RecipeMatrixRow } from '../../../../../core/models/recipe-matrix-row.model';

export interface RecipeMatrixCellUpdate {
  cell: RecipeMatrixCell;
  valueType: ParameterValueType;
  selectedOptionId?: number | null;
  valueJson?: string | null;
}

interface MatrixGroup {
  key: string;
  title: string;
  order: number;
  rows: RecipeMatrixRow[];
}

type MatrixCellVisualState =
  | 'golden-active'
  | 'golden-inactive'
  | 'editable-default'
  | 'computed'
  | 'user-modified'
  | 'readonly-neutral';

@Component({
  selector: 'app-recipe-matrix-view',
  standalone: true,
  imports: [NgIf, NgFor, NgClass, NgSwitch, NgSwitchCase, NgSwitchDefault, FormsModule],
  templateUrl: './recipe-matrix-view.component.html',
  styleUrl: './recipe-matrix-view.component.scss'
})
/**
 * RecipeMatrixViewComponent coordinates UI logic for this feature.
 */
export class RecipeMatrixViewComponent {
  @Input()
  set matrix(value: RecipeMatrix | null) {
    this._matrix = value;
    this.rebuildGroups();
  }

  get matrix(): RecipeMatrix | null {
    return this._matrix;
  }

  @Output() addStepClicked = new EventEmitter<void>();
  @Output() cellUpdated = new EventEmitter<RecipeMatrixCellUpdate>();

  readonly enumValueType: ParameterValueType = 'ENUM';
  readonly numberValueType: ParameterValueType = 'NUMBER';
  groupedRows: MatrixGroup[] = [];
  pinnedRows: RecipeMatrixRow[] = [];

  private _matrix: RecipeMatrix | null = null;
  private collapsedByGroupKey: Record<string, boolean> = {};
  private readonly pinnedParameterOrder: string[] = [
    'name',
    'chambers',
    'mode',
    'max time',
    'rtc mode'
  ];

  get stepColumns() {
    return this.matrix?.columns ?? [];
  }

  get parameterRows() {
    return this.matrix?.rows ?? [];
  }

  private rebuildGroups(): void {
    const rows = this.parameterRows;
    if (rows.length === 0) {
      this.pinnedRows = [];
      this.groupedRows = [];
      return;
    }

    const pinnedNameToRank = new Map<string, number>(
      this.pinnedParameterOrder.map((name, index) => [name, index])
    );

    const pinnedRows: RecipeMatrixRow[] = [];
    const nonPinnedRows: RecipeMatrixRow[] = [];

    for (const row of rows) {
      const normalizedName = (row.parameterName ?? '').trim().toLowerCase();
      if (pinnedNameToRank.has(normalizedName)) {
        pinnedRows.push(row);
      } else {
        nonPinnedRows.push(row);
      }
    }

    this.pinnedRows = pinnedRows.sort((a, b) => {
      const aRank = pinnedNameToRank.get((a.parameterName ?? '').trim().toLowerCase()) ?? Number.MAX_SAFE_INTEGER;
      const bRank = pinnedNameToRank.get((b.parameterName ?? '').trim().toLowerCase()) ?? Number.MAX_SAFE_INTEGER;
      return aRank - bRank;
    });

    const groupedMap = new Map<string, MatrixGroup>();
    const ungroupedKey = 'ungrouped';
    const ungroupedTitle = 'Other Parameters';

    for (const row of nonPinnedRows) {
      const hasGroup = Boolean(row.parameterGroup && row.parameterGroup.trim().length > 0);
      const groupTitle = hasGroup ? row.parameterGroup!.trim() : ungroupedTitle;
      const key = hasGroup ? groupTitle.toLowerCase() : ungroupedKey;
      const order = hasGroup ? Number(row.parameterGroupOrder ?? 0) : Number.MAX_SAFE_INTEGER;

      if (!groupedMap.has(key)) {
        groupedMap.set(key, {
          key,
          title: groupTitle,
          order,
          rows: []
        });
      }

      groupedMap.get(key)?.rows.push(row);
    }

    this.groupedRows = Array.from(groupedMap.values())
      .filter((group) => group.rows.length > 0)
      .sort((a, b) => (a.order - b.order) || a.title.localeCompare(b.title));

    const existingKeys = new Set(this.groupedRows.map((group) => group.key));
    Object.keys(this.collapsedByGroupKey).forEach((key) => {
      if (!existingKeys.has(key)) {
        delete this.collapsedByGroupKey[key];
      }
    });
  }

  toggleGroup(groupKey: string): void {
    this.collapsedByGroupKey[groupKey] = !this.isGroupCollapsed(groupKey);
  }

  isGroupCollapsed(groupKey: string): boolean {
    return this.collapsedByGroupKey[groupKey] ?? false;
  }

  /**
   * Handles the isEditable workflow.
   */
  isEditable(cell: RecipeMatrixCell): boolean {
    const isActive = !cell.activationState || cell.activationState === 'ENABLED';
    return Boolean(cell.editable)
      && !cell.lockedByGolden
      && !cell.computed
      && Boolean(cell.stepParameterId)
      && isActive;
  }

  computeCellDisplayState(cell: RecipeMatrixCell): MatrixCellVisualState {
    if (cell.lockedByGolden) {
      return cell.activationState === 'DISABLED' ? 'golden-inactive' : 'golden-active';
    }

    if (cell.computed) {
      return 'computed';
    }

    if (cell.editable && cell.userModified) {
      return 'user-modified';
    }

    if (cell.editable) {
      return 'editable-default';
    }

    return 'readonly-neutral';
  }

  getCellStateClass(cell: RecipeMatrixCell): string {
    const visualState = this.computeCellDisplayState(cell);
    return `cell--${visualState}`;
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

    const normalizedValueJson = this.toValueJsonPayload(cell.valueType, rawValue);

    this.cellUpdated.emit({
      cell,
      valueType: cell.valueType,
      valueJson: normalizedValueJson,
      selectedOptionId: null
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

  getInputDisplayValue(cell: RecipeMatrixCell): string {
    return this.normalizeJsonScalarForDisplay(cell.valueJson);
  }

  getReadonlyDisplayValue(cell: RecipeMatrixCell): string {
    if (cell.displayValue != null && cell.displayValue !== '') {
      return this.normalizeJsonScalarForDisplay(cell.displayValue);
    }
    return this.normalizeJsonScalarForDisplay(cell.valueJson);
  }

  private toValueJsonPayload(valueType: ParameterValueType, rawValue: string | null | undefined): string | null {
    const value = rawValue ?? '';

    if (valueType === 'STRING') {
      return JSON.stringify(value);
    }

    return value;
  }

  private normalizeJsonScalarForDisplay(rawValue: string | null | undefined): string {
    if (rawValue == null) {
      return '';
    }

    const trimmed = rawValue.trim();
    if (!trimmed) {
      return '';
    }

    try {
      const parsed = JSON.parse(trimmed);
      if (parsed == null) {
        return '';
      }
      if (typeof parsed === 'string' || typeof parsed === 'number' || typeof parsed === 'boolean') {
        return String(parsed);
      }
      return rawValue;
    } catch {
      return rawValue;
    }
  }
}
