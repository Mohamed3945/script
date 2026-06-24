import { NgFor, NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { StepParameterGridRow } from '../../../../../core/models/step-parameter-grid-row.model';

@Component({
  selector: 'app-recipe-matrix-view',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './recipe-matrix-view.component.html',
  styleUrl: './recipe-matrix-view.component.scss'
})
export class RecipeMatrixViewComponent {
  @Input() rows: StepParameterGridRow[] = [];

  @Output() addParameterClicked = new EventEmitter<void>();

  get stepColumns(): { stepId: number; stepName: string; stepOrderIndex: number }[] {
    const map = new Map<number, { stepId: number; stepName: string; stepOrderIndex: number }>();

    for (const row of this.rows) {
      if (!map.has(row.stepId)) {
        map.set(row.stepId, {
          stepId: row.stepId,
          stepName: row.stepName,
          stepOrderIndex: row.stepOrderIndex
        });
      }
    }

    return Array.from(map.values()).sort((a, b) => a.stepOrderIndex - b.stepOrderIndex);
  }

  get parameterRows(): { parameterDefinitionName: string; values: Record<number, string> }[] {
    const map = new Map<string, { parameterDefinitionName: string; values: Record<number, string> }>();

    for (const row of this.rows) {
      const key = row.parameterDefinitionName;

      if (!map.has(key)) {
        map.set(key, {
          parameterDefinitionName: row.parameterDefinitionName,
          values: {}
        });
      }

      const target = map.get(key)!;
      target.values[row.stepId] = row.selectedOptionLabel || row.valueJson || '-';
    }

    return Array.from(map.values()).sort((a, b) =>
      a.parameterDefinitionName.localeCompare(b.parameterDefinitionName)
    );
  }
}
