import { NgClass, NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, map, Observable, of } from 'rxjs';
import { EndpointOperator , OPERATOR_LABELS } from '../../../../../core/models/endpoint-operator.model';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ParameterOption } from '../../../../../core/models/parameter-option.model';
import { ParameterValueType } from '../../../../../core/models/parameter-value-type.model';
import { StepEndpoint, StepEndpointCondition } from '../../../../../core/models/step-endpoint.model';
import { ParameterDefinitionApiService } from '../../../../../core/services/parameter-definition-api.service';
import { ParameterOptionApiService } from '../../../../../core/services/parameter-option-api.service';
import { RecipeApiService } from '../../../../../core/services/recipe-api.service';

interface EditableCondition {
  id: number | null;
  endpointParameterId: number | null;
  operator: EndpointOperator;
  valueType: ParameterValueType | null;
  valueInput: string;
  selectedOptionId: number | null;
  availableOptions: ParameterOption[];
}

@Component({
  selector: 'app-step-endpoint-modal',
  standalone: true,
  imports: [NgIf, NgFor, NgClass, NgSwitch, NgSwitchCase, NgSwitchDefault, FormsModule],
  templateUrl: './step-endpoint-modal.component.html',
  styleUrl: './step-endpoint-modal.component.scss'
})
export class StepEndpointModalComponent implements OnChanges {
  @Input() visible = false;
  @Input() stepId: number | null = null;
  @Input() workspaceMode: 'golden' | 'derived' = 'derived';
  @Input() lockedByGolden = false;

  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  readonly operatorOptions: EndpointOperator[] = ['EQ', 'NEQ', 'LT', 'LTE', 'GT', 'GTE'];
  readonly operatorLabels = OPERATOR_LABELS;

  endpointDefinitions: ParameterDefinition[] = [];
  conditions: EditableCondition[] = [];
  clause: 'AND' | 'OR' = 'AND';

  loading = false;
  saving = false;
  deleting = false;
  errorMessage: string | null = null;
  conditionDropActive = false;

  private endpointSnapshot: StepEndpoint | null = null;
  private loadedStepId: number | null = null;
  private loadToken = 0;
  private readonly optionsByDefinitionId = new Map<number, ParameterOption[]>();

  constructor(
    private recipeApiService: RecipeApiService,
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private parameterOptionApiService: ParameterOptionApiService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    const becameVisible = Boolean(changes['visible']?.currentValue) && !changes['visible']?.previousValue;
    const stepChanged = this.visible && this.stepId != null && this.stepId !== this.loadedStepId;

    if (becameVisible || stepChanged) {
      this.loadModalData();
    }

    if (!this.visible) {
      this.errorMessage = null;
    }
  }

  get isReadOnly(): boolean {
    return this.workspaceMode === 'derived' && this.lockedByGolden;
  }

  get canDeleteEndpoint(): boolean {
    return Boolean(this.endpointSnapshot?.id);
  }

  get availableDefinitionsForNewCondition(): ParameterDefinition[] {
    const usedIds = this.getUsedDefinitionIds();
    return this.endpointDefinitions.filter((definition) => definition.id != null && !usedIds.has(definition.id));
  }

  get canAddCondition(): boolean {
    return !this.isReadOnly && this.availableDefinitionsForNewCondition.length > 0;
  }

  close(): void {
    if (this.saving || this.deleting) {
      return;
    }
    this.closed.emit();
  }

  addCondition(): void {
    if (!this.canAddCondition) {
      return;
    }

    const preferred = this.availableDefinitionsForNewCondition[0];
    const condition = this.createCondition(preferred);

    this.conditions = [...this.conditions, condition];
    this.ensureOptionsLoaded(condition);
    this.errorMessage = null;
  }

  onDefinitionDragStart(event: DragEvent, definition: ParameterDefinition): void {
    if (this.isReadOnly || definition.id == null || !event.dataTransfer) {
      return;
    }

    if (this.isDefinitionAlreadyUsed(definition.id)) {
      event.preventDefault();
      return;
    }

    event.dataTransfer.setData('text/plain', String(definition.id));
    event.dataTransfer.effectAllowed = 'copy';
  }

  onConditionPanelDragOver(event: DragEvent): void {
    if (this.isReadOnly) {
      return;
    }

    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'copy';
    }
    this.conditionDropActive = true;
  }

  onConditionPanelDragLeave(): void {
    this.conditionDropActive = false;
  }

  onConditionPanelDrop(event: DragEvent): void {
    if (this.isReadOnly) {
      return;
    }

    event.preventDefault();
    this.conditionDropActive = false;

    const definition = this.extractDroppedDefinition(event);
    if (!definition) {
      return;
    }

    if (definition.id == null || this.isDefinitionAlreadyUsed(definition.id)) {
      this.errorMessage = 'This endpoint parameter is already used in another condition.';
      return;
    }

    const condition = this.createCondition(definition);
    this.conditions = [...this.conditions, condition];
    this.ensureOptionsLoaded(condition);
    this.errorMessage = null;
  }

  onConditionDefinitionDrop(event: DragEvent, condition: EditableCondition): void {
    if (this.isReadOnly) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();

    const definition = this.extractDroppedDefinition(event);
    if (!definition) {
      return;
    }

    if (definition.id == null) {
      return;
    }

    if (this.isDefinitionAlreadyUsed(definition.id, condition)) {
      this.errorMessage = 'This endpoint parameter is already used in another condition.';
      return;
    }

    this.onEndpointParameterChanged(condition, definition.id ?? null);
  }

  allowDropOnCondition(event: DragEvent): void {
    if (this.isReadOnly) {
      return;
    }

    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'copy';
    }
  }

  removeCondition(index: number): void {
    if (this.isReadOnly) {
      return;
    }

    this.conditions = this.conditions.filter((_, i) => i !== index);
  }

  onEndpointParameterChanged(condition: EditableCondition, rawDefinitionId: number | string | null): void {
    if (this.isReadOnly) {
      return;
    }

    const definitionId = rawDefinitionId == null || rawDefinitionId === '' ? null : Number(rawDefinitionId);

    if (definitionId != null && this.isDefinitionAlreadyUsed(definitionId, condition)) {
      this.errorMessage = 'This endpoint parameter is already used in another condition.';
      return;
    }

    const definition = this.endpointDefinitions.find((candidate) => candidate.id === definitionId) ?? null;

    condition.endpointParameterId = definition?.id ?? null;
    condition.valueType = definition?.valueType ?? null;
    condition.selectedOptionId = null;
    condition.valueInput = '';
    condition.availableOptions = [];

    if (condition.valueType === 'ENUM') {
      condition.operator = 'EQ';
      this.ensureOptionsLoaded(condition);
    }

    this.errorMessage = null;
  }

  getSelectableDefinitions(condition: EditableCondition): ParameterDefinition[] {
    return this.endpointDefinitions.filter((definition) => {
      if (definition.id == null) {
        return false;
      }

      if (condition.endpointParameterId === definition.id) {
        return true;
      }

      return !this.isDefinitionAlreadyUsed(definition.id, condition);
    });
  }

  save(): void {
    if (this.isReadOnly || this.stepId == null || this.saving) {
      return;
    }

    const payloadConditions: StepEndpointCondition[] = [];
    const usedDefinitionIds = new Set<number>();

    for (let index = 0; index < this.conditions.length; index += 1) {
      const condition = this.conditions[index];
      const endpointParameterId = condition.endpointParameterId;
      const valueType = condition.valueType;

      if (!endpointParameterId || !valueType) {
        this.errorMessage = 'Every condition must reference an endpoint parameter.';
        return;
      }

      if (usedDefinitionIds.has(endpointParameterId)) {
        this.errorMessage = 'A parameter cannot be used in more than one condition.';
        return;
      }
      usedDefinitionIds.add(endpointParameterId);

      const dto: StepEndpointCondition = {
        id: condition.id,
        endpointParameterId,
        operator: condition.operator,
        orderIndex: index,
        valueJson: null,
        selectedOptionId: null
      };

      if (valueType === 'ENUM') {
        if (condition.selectedOptionId == null) {
          this.errorMessage = 'Every ENUM condition must select an option.';
          return;
        }
        dto.selectedOptionId = condition.selectedOptionId;
      } else {
        const normalized = this.toValueJsonPayload(valueType, condition.valueInput);
        if (normalized == null || normalized.trim().length === 0) {
          this.errorMessage = 'Every condition must provide a comparison value.';
          return;
        }
        dto.valueJson = normalized;
      }

      payloadConditions.push(dto);
    }

    const payload: StepEndpoint = {
      id: this.endpointSnapshot?.id ?? null,
      stepId: this.stepId,
      clause: payloadConditions.length > 1 ? this.clause : null,
      conditions: payloadConditions
    };

    this.errorMessage = null;
    this.saving = true;

    this.recipeApiService.upsertStepEndpoint(this.stepId, payload).subscribe({
      next: () => {
        this.saving = false;
        this.saved.emit();
        this.closed.emit();
      },
      error: (error) => {
        console.error('Failed to save step endpoint', error);
        this.errorMessage = 'Failed to save endpoint.';
        this.saving = false;
      }
    });
  }

  deleteEndpoint(): void {
    if (this.isReadOnly || this.stepId == null || !this.canDeleteEndpoint || this.deleting) {
      return;
    }

    const confirmed = window.confirm('Delete this endpoint and revert to time-based stop?');
    if (!confirmed) {
      return;
    }

    this.errorMessage = null;
    this.deleting = true;

    this.recipeApiService.deleteStepEndpoint(this.stepId).subscribe({
      next: () => {
        this.deleting = false;
        this.saved.emit();
        this.closed.emit();
      },
      error: (error) => {
        console.error('Failed to delete step endpoint', error);
        this.errorMessage = 'Failed to delete endpoint.';
        this.deleting = false;
      }
    });
  }

  private loadModalData(): void {
    if (!this.visible || this.stepId == null) {
      return;
    }

    const token = ++this.loadToken;
    this.loading = true;
    this.errorMessage = null;
    this.conditionDropActive = false;
    this.optionsByDefinitionId.clear();

    forkJoin({
      endpoint: this.recipeApiService.getStepEndpoint(this.stepId),
      endpointDefinitions: this.parameterDefinitionApiService.getDefinitions('ENDPOINT')
    }).subscribe({
      next: ({ endpoint, endpointDefinitions }) => {
        if (token !== this.loadToken) {
          return;
        }

        this.loadedStepId = this.stepId;
        this.endpointSnapshot = endpoint;
        this.endpointDefinitions = [...endpointDefinitions].sort((a, b) => {
          const aRank = a.orderIndexInGroup ?? 0;
          const bRank = b.orderIndexInGroup ?? 0;
          return aRank - bRank;
        });

        this.clause = endpoint.clause === 'OR' ? 'OR' : 'AND';
        this.conditions = this.hydrateConditions(endpoint.conditions ?? []);

        this.loadConditionOptions(this.conditions, token);
      },
      error: (error) => {
        if (token !== this.loadToken) {
          return;
        }

        console.error('Failed to load endpoint modal data', error);
        this.errorMessage = 'Failed to load endpoint data.';
        this.endpointSnapshot = null;
        this.endpointDefinitions = [];
        this.conditions = [];
        this.loading = false;
      }
    });
  }

  private loadConditionOptions(conditions: EditableCondition[], token: number): void {
    const enumConditions = conditions.filter((condition) => condition.valueType === 'ENUM' && condition.endpointParameterId != null);

    if (enumConditions.length === 0) {
      this.loading = false;
      return;
    }

    const requests = enumConditions.map((condition) => this.loadOptionsForDefinition(condition.endpointParameterId as number).pipe(
      map((options) => {
        condition.availableOptions = options;
      })
    ));

    forkJoin(requests).subscribe({
      next: () => {
        if (token === this.loadToken) {
          this.loading = false;
        }
      },
      error: (error) => {
        if (token !== this.loadToken) {
          return;
        }

        console.error('Failed to load endpoint enum options', error);
        this.errorMessage = 'Some endpoint option lists could not be loaded.';
        this.loading = false;
      }
    });
  }

  private ensureOptionsLoaded(condition: EditableCondition): void {
    if (condition.valueType !== 'ENUM' || condition.endpointParameterId == null) {
      return;
    }

    this.loadOptionsForDefinition(condition.endpointParameterId).subscribe({
      next: (options) => {
        condition.availableOptions = options;
      },
      error: (error) => {
        console.error('Failed to load endpoint options', error);
        condition.availableOptions = [];
      }
    });
  }

  private loadOptionsForDefinition(definitionId: number): Observable<ParameterOption[]> {
    const cached = this.optionsByDefinitionId.get(definitionId);
    if (cached) {
      return of(cached);
    }

    return this.parameterOptionApiService.getOptionsByDefinition(definitionId).pipe(
      map((options) => {
        const sorted = [...options].sort((a, b) => a.orderIndex - b.orderIndex);
        this.optionsByDefinitionId.set(definitionId, sorted);
        return sorted;
      })
    );
  }

  private hydrateConditions(conditions: StepEndpointCondition[]): EditableCondition[] {
    return [...conditions]
      .sort((a, b) => a.orderIndex - b.orderIndex)
      .map((condition) => {
        const definition = this.endpointDefinitions.find((candidate) => candidate.id === condition.endpointParameterId) ?? null;
        const valueType = condition.endpointParameterValueType ?? definition?.valueType ?? null;

        return {
          id: condition.id,
          endpointParameterId: condition.endpointParameterId,
          operator: condition.operator,
          valueType,
          valueInput: this.toInputValue(valueType, condition.valueJson),
          selectedOptionId: condition.selectedOptionId,
          availableOptions: []
        };
      });
  }

  private createCondition(definition: ParameterDefinition): EditableCondition {
    return {
      id: null,
      endpointParameterId: definition.id ?? null,
      operator: 'EQ',
      valueType: definition.valueType,
      valueInput: '',
      selectedOptionId: null,
      availableOptions: []
    };
  }

  private isDefinitionAlreadyUsed(definitionId: number, exceptCondition?: EditableCondition): boolean {
    return this.conditions.some((condition) => condition !== exceptCondition && condition.endpointParameterId === definitionId);
  }

  private getUsedDefinitionIds(exceptCondition?: EditableCondition): Set<number> {
    const ids = new Set<number>();

    for (const condition of this.conditions) {
      if (condition === exceptCondition) {
        continue;
      }

      if (condition.endpointParameterId != null) {
        ids.add(condition.endpointParameterId);
      }
    }

    return ids;
  }

  private extractDroppedDefinition(event: DragEvent): ParameterDefinition | null {
    const payload = event.dataTransfer?.getData('text/plain')?.trim() ?? '';
    if (!payload) {
      return null;
    }

    const definitionId = Number(payload);
    if (!Number.isFinite(definitionId)) {
      return null;
    }

    return this.endpointDefinitions.find((definition) => definition.id === definitionId) ?? null;
  }

  private toInputValue(valueType: ParameterValueType | null, rawValue: string | null): string {
    if (rawValue == null) {
      return '';
    }

    if (valueType === 'STRING') {
      try {
        const parsed = JSON.parse(rawValue);
        return typeof parsed === 'string' ? parsed : rawValue;
      } catch {
        return rawValue;
      }
    }

    return rawValue;
  }

  private toValueJsonPayload(valueType: ParameterValueType, rawValue: string): string | null {
    const trimmed = (rawValue ?? '').trim();
    if (!trimmed) {
      return null;
    }

    if (valueType === 'STRING') {
      return JSON.stringify(trimmed);
    }

    if (valueType === 'NUMBER') {
      const asNumber = Number(trimmed);
      if (Number.isNaN(asNumber)) {
        return null;
      }
      return String(asNumber);
    }

    return trimmed;
  }
}
