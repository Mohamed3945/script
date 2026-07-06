import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { Recipe } from '../../../../../core/models/recipe.model';
import { RecipeKind } from '../../../../../core/models/recipe-kind.model';

@Component({
  selector: 'app-recipe-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgFor],
  templateUrl: './recipe-form.component.html',
  styleUrl: './recipe-form.component.scss'
})
/**
 * RecipeFormComponent coordinates UI logic for this feature.
 */
export class RecipeFormComponent implements OnInit {
  @Input() initialValue: Recipe | null = null;
  @Input() mode: 'create' | 'derived' | 'edit' = 'create';

  @Output() submitted = new EventEmitter<{ recipe: Recipe; resultProfileId?: number }>();

  readonly recipeKinds: RecipeKind[] = ['GOLDEN', 'DERIVED', 'IMPORTED'];
  readonly recipeStatuses = ['DRAFT', 'VALIDATED', 'ARCHIVED'];

  form: ReturnType<FormBuilder['group']>;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      recipeKind: ['GOLDEN' as RecipeKind, Validators.required],
      parentRecipeId: [null as number | null],
      resultProfileId: [null as number | null],
      name: ['', Validators.required],
      description: [''],
      creatorId: [null as number | null, Validators.required],
      revisorId: [null as number | null],
      processFamily: [''],
      status: ['DRAFT', Validators.required],
      version: [null as number | null],
      frozen: [false]
    });
  }

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    if (this.initialValue) {
      this.form.patchValue({
        recipeKind: this.initialValue.recipeKind,
        parentRecipeId: this.initialValue.parentRecipeId ?? null,
        name: this.initialValue.name,
        description: this.initialValue.description ?? '',
        creatorId: this.initialValue.creatorId,
        revisorId: this.initialValue.revisorId ?? null,
        processFamily: this.initialValue.processFamily ?? '',
        status: this.initialValue.status,
        version: this.initialValue.version ?? null,
        frozen: this.initialValue.frozen
      });
    }

    if (this.mode === 'derived') {
      this.form.patchValue({ recipeKind: 'DERIVED' });
      this.form.get('recipeKind')?.disable();
    }
  }

  get isDerived(): boolean {
    return (this.form.getRawValue().recipeKind as RecipeKind) === 'DERIVED';
  }

  get isGolden(): boolean {
    return (this.form.getRawValue().recipeKind as RecipeKind) === 'GOLDEN';
  }

  /**
   * Handles the onSubmit workflow.
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    const recipe: Recipe = {
      recipeKind: raw.recipeKind as RecipeKind,
      parentRecipeId: raw.parentRecipeId,
      name: raw.name || '',
      description: raw.description || null,
      creatorId: raw.creatorId!,
      revisorId: raw.revisorId,
      processFamily: raw.processFamily || null,
      status: raw.status as Recipe['status'],
      version: raw.version,
      frozen: !!raw.frozen
    };

    this.submitted.emit({
      recipe,
      resultProfileId: raw.resultProfileId ?? undefined
    });
  }
}
