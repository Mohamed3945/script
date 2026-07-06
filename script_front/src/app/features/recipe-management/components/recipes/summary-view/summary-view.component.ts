import { KeyValuePipe, NgFor, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Recipe } from '../../../../../core/models/recipe.model';
import { RecipeKind } from '../../../../../core/models/recipe-kind.model';
import { RecipeStatus } from '../../../../../core/models/recipe-status.model';

@Component({
  selector: 'app-summary-view',
  standalone: true,
  imports: [NgFor, NgSwitch, NgSwitchCase, NgSwitchDefault, KeyValuePipe],
  templateUrl: './summary-view.component.html',
  styleUrl: './summary-view.component.scss'
})
/**
 * SummaryViewComponent renders recipe passport fields dynamically.
 */
export class SummaryViewComponent {
  @Input() recipe!: Recipe;
  @Input() savingField: string | null = null;
  @Input() recipeKindOptions: RecipeKind[] = [];
  @Input() recipeStatusOptions: RecipeStatus[] = [];

  @Output() fieldCommitted = new EventEmitter<{ key: string; value: unknown }>();

  keepFieldOrder = (): number => 0;

  summaryLabel(fieldKey: string): string {
    return fieldKey
      .replace(/([a-z])([A-Z])/g, '$1 $2')
      .replace(/\bId\b/g, 'ID')
      .replace(/^./, (match) => match.toUpperCase());
  }

  isFieldEditable(fieldKey: string): boolean {
    return fieldKey !== 'id';
  }

  editorType(fieldKey: string, fieldValue: unknown): 'kind' | 'status' | 'boolean' | 'number' | 'textarea' | 'text' {
    if (fieldKey === 'recipeKind') {
      return 'kind';
    }

    if (fieldKey === 'status') {
      return 'status';
    }

    if (typeof fieldValue === 'boolean') {
      return 'boolean';
    }

    if (typeof fieldValue === 'number' || fieldKey.endsWith('Id') || fieldKey === 'version') {
      return 'number';
    }

    if (fieldKey === 'description') {
      return 'textarea';
    }

    return 'text';
  }

  commitTextOrNumber(fieldKey: string, rawValue: string): void {
    if (!this.isFieldEditable(fieldKey)) {
      return;
    }

    const currentValue = (this.recipe as unknown as Record<string, unknown>)[fieldKey];
    const trimmed = (rawValue ?? '').trim();
    let nextValue: unknown;

    if (typeof currentValue === 'number' || fieldKey.endsWith('Id') || fieldKey === 'version') {
      if (trimmed.length === 0) {
        nextValue = null;
      } else {
        const asNumber = Number(trimmed);
        if (Number.isNaN(asNumber)) {
          return;
        }
        nextValue = asNumber;
      }
    } else {
      nextValue = trimmed.length === 0 ? null : trimmed;
    }

    this.fieldCommitted.emit({ key: fieldKey, value: nextValue });
  }

  commitSelect(fieldKey: string, rawValue: string): void {
    if (!this.isFieldEditable(fieldKey)) {
      return;
    }

    this.fieldCommitted.emit({ key: fieldKey, value: rawValue });
  }

  commitBoolean(fieldKey: string, checked: boolean): void {
    if (!this.isFieldEditable(fieldKey)) {
      return;
    }

    this.fieldCommitted.emit({ key: fieldKey, value: checked });
  }
}
