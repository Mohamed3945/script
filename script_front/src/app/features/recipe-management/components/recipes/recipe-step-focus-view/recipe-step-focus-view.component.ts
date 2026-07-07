import { NgFor, NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { Step } from '../../../../../core/models/step.model';
import { StepParameter } from '../../../../../core/models/step-parameter.model';
import { StepListComponent } from '../../steps/step-list/step-list.component';

interface StepParameterGroup {
  key: string;
  title: string;
  order: number;
  parameters: StepParameter[];
}

@Component({
  selector: 'app-recipe-step-focus-view',
  standalone: true,
  imports: [NgIf, NgFor, StepListComponent],
  templateUrl: './recipe-step-focus-view.component.html',
  styleUrl: './recipe-step-focus-view.component.scss'
})
/**
 * RecipeStepFocusViewComponent coordinates UI logic for this feature.
 */
export class RecipeStepFocusViewComponent {
  @Input() steps: Step[] = [];
  @Input() selectedStepId?: number | null;
  @Input() selectedStep: Step | null = null;
  @Input()
  set parameters(value: StepParameter[]) {
    this._parameters = value ?? [];
    this.rebuildGroups();
  }

  get parameters(): StepParameter[] {
    return this._parameters;
  }

  @Output() stepSelected = new EventEmitter<Step>();
  @Output() addParameterClicked = new EventEmitter<void>();
  @Output() deleteParameterClicked = new EventEmitter<StepParameter>();

  groupedParameters: StepParameterGroup[] = [];

  private _parameters: StepParameter[] = [];
  private collapsedByGroupKey: Record<string, boolean> = {};

  private rebuildGroups(): void {
    const groups = new Map<string, StepParameterGroup>();
    const ungroupedKey = 'ungrouped';
    const ungroupedTitle = 'Other Parameters';

    for (const parameter of this._parameters) {
      const hasGroup = Boolean(parameter.parameterGroup && parameter.parameterGroup.trim().length > 0);
      const title = hasGroup ? parameter.parameterGroup!.trim() : ungroupedTitle;
      const key = hasGroup ? title.toLowerCase() : ungroupedKey;
      const order = hasGroup ? Number(parameter.parameterGroupOrder ?? 0) : Number.MAX_SAFE_INTEGER;

      if (!groups.has(key)) {
        groups.set(key, {
          key,
          title,
          order,
          parameters: []
        });
      }

      groups.get(key)?.parameters.push(parameter);
    }

    this.groupedParameters = Array.from(groups.values())
      .sort((a, b) => (a.order - b.order) || a.title.localeCompare(b.title));

    const existingKeys = new Set(this.groupedParameters.map((group) => group.key));
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

  constructor(private router: Router) {}

  /**
   * Handles the goToDefinition workflow.
   */
  goToDefinition(parameter: StepParameter): void {
    if (!parameter.definitionId) return;
    this.router.navigate(['/parameter', parameter.definitionId]);
  }
}
