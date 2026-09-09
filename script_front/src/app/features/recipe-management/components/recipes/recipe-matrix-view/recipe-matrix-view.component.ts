import { NgClass, NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParameterValueType } from '../../../../../core/models/parameter-value-type.model';
import { RecipeMatrixEndpointCell } from '../../../../../core/models/recipe-matrix-endpoint-cell.model';
import { RecipeMatrix } from '../../../../../core/models/recipe-matrix.model';
import { RecipeMatrixCell } from '../../../../../core/models/recipe-matrix-cell.model';
import { RecipeMatrixRow } from '../../../../../core/models/recipe-matrix-row.model';
import { OPERATOR_LABELS, EndpointOperator } from '../../../../../core/models/endpoint-operator.model';

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

export interface RecipeMatrixEndpointCellContextRequest {
  endpointCell: RecipeMatrixEndpointCell;
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
  | 'computed-pending'
  | 'computed-from-modified'
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
  @Input() endpointRow: RecipeMatrixEndpointCell[] | null = null;
  @Input()
  set recomputedPulseStepParameterIds(value: number[] | null) {
    this.recomputedPulseStepParameterIdsSnapshot = (value ?? []).filter((id) => Number.isFinite(id));
  }
  @Input()
  set recomputedPulseTick(value: number) {
    if (!Number.isFinite(value) || value === this.lastPulseTick) {
      return;
    }
    this.lastPulseTick = value;
    this.triggerRecomputedPulse();
  }

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
  @Output() endpointCellContextRequested = new EventEmitter<RecipeMatrixEndpointCellContextRequest>();

  readonly enumValueType: ParameterValueType = 'ENUM';
  readonly numberValueType: ParameterValueType = 'NUMBER';

  groupedRows: MatrixGroup[] = [];
  pinnedRows: RecipeMatrixRow[] = [];
  detailsTooltipVisible = false;
  detailsTooltipX = 0;
  detailsTooltipY = 0;

  private _matrix: RecipeMatrix | null = null;
  private lastRenderableMatrix: RecipeMatrix | null = null;
  private recomputedPulseStepParameterIdsSnapshot: number[] = [];
  private recomputedPulseStepParameterIdSet = new Set<number>();
  private lastPulseTick = 0;
  private pulseClearTimer: ReturnType<typeof setTimeout> | null = null;
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
    return this.stepColumns.length > 0 && (this.parameterRows.length > 0 || this.endpointCells.length > 0);
  }

  get isGoldenWorkspace(): boolean {
    return this.workspaceMode === 'golden';
  }

  get isDerivedWorkspace(): boolean {
    return this.workspaceMode === 'derived';
  }

  get endpointCells(): RecipeMatrixEndpointCell[] {
    const source = this.endpointRow ?? this.effectiveMatrix?.endpointRow ?? null;
    if (source && source.length > 0) {
      return source;
    }

    return this.stepColumns.map((column) => ({
      stepId: column.stepId,
      endpointId: null,
      clause: null,
      conditionCount: 0,
      summaryLabel: '',
      lockedByGolden: false
    }));
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
      const hasExplicitGroup = Boolean(row.parameterGroup && row.parameterGroup.trim().length > 0);
      const normalizedName = (row.parameterName ?? '').trim().toLowerCase();
      if (!hasExplicitGroup && pinnedNameToRank.has(normalizedName)) {
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
    if (groupKey === 'ungrouped') {
      return;
    }
    this.collapsedByGroupKey[groupKey] = !this.isGroupCollapsed(groupKey);
  }

  isGroupCollapsed(groupKey: string): boolean {
    if (groupKey === 'ungrouped') {
      return false;
    }
    return this.collapsedByGroupKey[groupKey] ?? true;
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
    if (!row.definitionId) {
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

  onParameterRowClick(event: MouseEvent, row: RecipeMatrixRow): void {
    if (!row.definitionId) {
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

  onParameterRowHoverStart(event: MouseEvent): void {
    this.detailsTooltipVisible = true;
    this.updateDetailsTooltipPosition(event);
  }

  onParameterRowHoverMove(event: MouseEvent): void {
    if (!this.detailsTooltipVisible) {
      return;
    }

    this.updateDetailsTooltipPosition(event);
  }

  onParameterRowHoverEnd(): void {
    this.detailsTooltipVisible = false;
  }

  onEndpointCellContextMenu(event: MouseEvent, endpointCell: RecipeMatrixEndpointCell): void {
    if (!endpointCell?.stepId) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();

    this.endpointCellContextRequested.emit({
      endpointCell,
      x: event.clientX,
      y: event.clientY
    });
  }

  getEndpointSummary(endpointCell: RecipeMatrixEndpointCell): string {
    if (endpointCell.conditionCount <= 0) {
      return '-';
    }

    const summary = (endpointCell.summaryLabel ?? '').trim();
    if (!summary) {
      return `${endpointCell.conditionCount} condition(s)`;
    }
    return this.formatEndpointSummary(summary)
  }

  computeCellDisplayState(cell: RecipeMatrixCell): MatrixCellVisualState {
    if (cell.lockedByGolden) {
      return cell.activationState === 'DISABLED' ? 'golden-inactive' : 'golden-active';
    }

    if (cell.computed) {
      if (cell.computedFromModified || cell.userModified) {
        return 'computed-from-modified';
      }
      return 'computed-pending';
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

  getCellPulseClass(cell: RecipeMatrixCell): string {
    if (cell.stepParameterId == null) {
      return '';
    }
    return this.recomputedPulseStepParameterIdSet.has(cell.stepParameterId) ? 'cell--recomputed-pulse' : '';
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

  trackByEndpointCell = (_: number, endpointCell: RecipeMatrixEndpointCell): string =>
    `endpoint:${endpointCell.stepId}`;

  get areAllGroupsCollapsed(): boolean {
    const collapsibleGroups = this.groupedRows.filter((group) => group.key !== 'ungrouped');
    return collapsibleGroups.length > 0 && collapsibleGroups.every((group) => this.isGroupCollapsed(group.key));
  }

  toggleAllGroups(): void {
    if (this.areAllGroupsCollapsed) {
      for (const group of this.groupedRows) {
        if (group.key !== 'ungrouped') {
          this.collapsedByGroupKey[group.key] = false;
        }
      }
      return;
    }

    for (const group of this.groupedRows) {
      if (group.key !== 'ungrouped') {
        this.collapsedByGroupKey[group.key] = true;
      }
    }
  }

  private buildRowKey(row: RecipeMatrixRow): string {
    return [
      row.parameterAlias ?? '',
      row.parameterName ?? '',
      row.parameterGroup ?? ''
    ].join('|');
  }

  private updateDetailsTooltipPosition(event: MouseEvent): void {
    const offsetX = 14;
    const offsetY = 16;
    const tooltipWidth = 120;
    const viewportWidth = typeof window !== 'undefined' ? window.innerWidth : 1280;

    this.detailsTooltipX = Math.max(8, Math.min(event.clientX + offsetX, viewportWidth - tooltipWidth));
    this.detailsTooltipY = event.clientY + offsetY;
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

  private formatEndpointSummary(summary: string): string {
    return summary
      .replace(/\bNEQ\b/g, OPERATOR_LABELS['NEQ' as EndpointOperator])
      .replace(/\bLTE\b/g, OPERATOR_LABELS['LTE' as EndpointOperator])
      .replace(/\bGTE\b/g, OPERATOR_LABELS['GTE' as EndpointOperator])
      .replace(/\bEQ\b/g, OPERATOR_LABELS['EQ' as EndpointOperator])
      .replace(/\bLT\b/g, OPERATOR_LABELS['LT' as EndpointOperator])
      .replace(/\bGT\b/g, OPERATOR_LABELS['GT' as EndpointOperator]);
  }

  private triggerRecomputedPulse(): void {
    if (this.pulseClearTimer) {
      clearTimeout(this.pulseClearTimer);
      this.pulseClearTimer = null;
    }

    this.recomputedPulseStepParameterIdSet = new Set(this.recomputedPulseStepParameterIdsSnapshot);
    if (this.recomputedPulseStepParameterIdSet.size === 0) {
      return;
    }

    this.pulseClearTimer = setTimeout(() => {
      this.recomputedPulseStepParameterIdSet.clear();
      this.pulseClearTimer = null;
    }, 950);
  }

}