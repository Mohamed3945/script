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
  action?: 'edit' | 'toggle-golden';
  lockedByGolden?: boolean;
}

export interface RecipeMatrixParameterRowContextRequest {
  definitionId: number;
  parameterName: string;
  x: number;
  y: number;
}

export interface RecipeMatrixStepContextRequest {
  stepId: number;
  stepCode?: string | null;
  x: number;
  y: number;
}

export interface RecipeMatrixCellContextRequest {
  cell: RecipeMatrixCell;
  x: number;
  y: number;
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
export class RecipeMatrixViewComponent {
  @Input() workspaceMode: 'golden' | 'derived' = 'derived';

  @Input()
  set matrix(value: RecipeMatrix | null) {
    this._matrix = value;

    if (this.hasRenderableMatrix(value)) {
      this.lastRenderableMatrix = value;
    }

    this.rebuildGroups();
  }

  get matrix(): RecipeMatrix | null {
    return this._matrix;
  }

  @Output() cellUpdated = new EventEmitter<RecipeMatrixCellUpdate>();
  @Output() stepHeaderContextRequested = new EventEmitter<RecipeMatrixStepContextRequest>();
  @Output() cellContextRequested = new EventEmitter<RecipeMatrixCellContextRequest>();
  @Output() parameterRowContextRequested = new EventEmitter<RecipeMatrixParameterRowContextRequest>();

  readonly enumValueType: ParameterValueType = 'ENUM';
  readonly numberValueType: ParameterValueType = 'NUMBER';

  groupedRows: MatrixGroup[] = [];
  pinnedRows: RecipeMatrixRow[] = [];

  private _matrix: RecipeMatrix | null = null;
  private lastRenderableMatrix: RecipeMatrix | null = null;
  private collapsedByGroupKey: Record<string, boolean> = {};
  private readonly pinnedParameterOrder: string[] = [
    'name',
    'chambers',
    'mode',
    'max time',
    'rtc mode'
  ];

  get effectiveMatrix(): RecipeMatrix | null {
    return this.hasRenderableMatrix(this._matrix)
      ? this._matrix
      : this.lastRenderableMatrix;
  }

  get stepColumns() {
    return this.effectiveMatrix?.columns ?? [];
  }

  get parameterRows() {
    return this.effectiveMatrix?.rows ?? [];
  }

  get hasDisplayableMatrix(): boolean {
    return this.stepColumns.length > 0 && this.parameterRows.length > 0;
  }

  get isGoldenWorkspace(): boolean {
    return this.workspaceMode === 'golden';
  }

  get isDerivedWorkspace(): boolean {
    return this.workspaceMode === 'derived';
  }

  private hasRenderableMatrix(matrix: RecipeMatrix | null): boolean {
    return Boolean(matrix && matrix.columns?.length && matrix.rows?.length);
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

  isEditable(cell: RecipeMatrixCell): boolean {
    const isActive = !cell.activationState || cell.activationState === 'ENABLED';
    const blockedByDerived = this.isDerivedWorkspace && cell.lockedByGolden;
    return Boolean(cell.editable)
      && !cell.computed
      && Boolean(cell.stepParameterId)
      && isActive
      && !blockedByDerived;
  }

  canToggleGolden(cell: RecipeMatrixCell): boolean {
    return this.isGoldenWorkspace && Boolean(cell.stepParameterId) && !cell.computed;
  }

  canOpenCellContext(cell: RecipeMatrixCell): boolean {
    return this.isGoldenWorkspace && Boolean(cell.stepParameterId);
  }

  onGoldenToggleRequested(cell: RecipeMatrixCell): void {
    if (!this.canToggleGolden(cell)) {
      return;
    }

    this.cellUpdated.emit({
      cell,
      valueType: cell.valueType,
      action: 'toggle-golden',
      lockedByGolden: !cell.lockedByGolden
    });
  }

  onStepHeaderContextMenu(event: MouseEvent, step: any): void {
    if (!this.isGoldenWorkspace || !step?.stepId) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();

    this.stepHeaderContextRequested.emit({
      stepId: step.stepId,
      stepCode: step.stepCode ?? null,
      x: event.clientX,
      y: event.clientY
    });
  }

  onCellContextMenu(event: MouseEvent, cell: RecipeMatrixCell): void {
    if (!this.canOpenCellContext(cell)) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();

    this.cellContextRequested.emit({
      cell,
      x: event.clientX,
      y: event.clientY
    });
  }

  onParameterRowContextMenu(event: MouseEvent, row: RecipeMatrixRow): void {
    if (!this.isGoldenWorkspace || !row.definitionId) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();

    this.parameterRowContextRequested.emit({
      definitionId: row.definitionId,
      parameterName: row.parameterName,
      x: event.clientX,
      y: event.clientY
    });
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

    const normalizedValueJson = this.toValueJsonPayload(cell.valueType, rawValue);

    this.cellUpdated.emit({
      cell,
      valueType: cell.valueType,
      valueJson: normalizedValueJson,
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

  getInputDisplayValue(cell: RecipeMatrixCell): string {
    return this.normalizeJsonScalarForDisplay(cell.valueJson);
  }

  getReadonlyDisplayValue(cell: RecipeMatrixCell): string {
    if (cell.displayValue != null && cell.displayValue !== '') {
      return this.normalizeJsonScalarForDisplay(cell.displayValue);
    }
    return this.normalizeJsonScalarForDisplay(cell.valueJson);
  }

  trackByStepColumn = (_: number, step: any): number | string =>
    step?.stepId ?? step?.stepCode ?? _;

  trackByPinnedRow = (_: number, row: RecipeMatrixRow): string =>
    this.buildRowKey(row);

  trackByGroup = (_: number, group: MatrixGroup): string =>
    group.key;

  trackByGroupRow = (_: number, row: RecipeMatrixRow): string =>
    this.buildRowKey(row);

  trackByCell = (_: number, cell: RecipeMatrixCell): string =>
    this.buildCellKey(cell);

  trackByOption = (_: number, option: any): number | string =>
    option?.id ?? option?.code ?? option?.label ?? _;

  private buildRowKey(row: RecipeMatrixRow): string {
    return [
      row.parameterAlias ?? '',
      row.parameterName ?? '',
      row.parameterGroup ?? ''
    ].join('|');
  }

  private buildCellKey(cell: RecipeMatrixCell): string {
    if (cell.stepParameterId != null) {
      return `sp:${cell.stepParameterId}`;
    }

    return `d:${cell.definitionId ?? 'na'}|s:${cell.stepId ?? 'na'}`;
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