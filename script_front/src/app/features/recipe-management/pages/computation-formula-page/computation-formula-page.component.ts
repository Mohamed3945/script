import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ComputationFormula } from '../../../../core/models/computation-formula.model';
import { RoundingPolicy } from '../../../../core/models/rounding-policy.model';
import { ComputationFormulaApiService } from '../../../../core/services/computation-formula-api.service';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
import { StepParameterGridRow } from '../../../../core/models/step-parameter-grid-row.model';

interface ParameterSelectorOption {
  key: string;
  label: string;
  stepCode: string;
  definitionPath: string;
}

interface MatrixColumnView {
  stepId: number;
  stepCode: string;
  stepName: string;
  stepOrderIndex: number;
}

interface MatrixCellView {
  key: string;
  stepCode: string;
  definitionPath: string;
  parameterLabel: string;
}

interface MatrixRowView {
  key: string;
  parameterLabel: string;
  parameterGroup: string;
  parameterGroupOrder: number;
  definitionPath: string;
  cellsByStepId: Record<number, MatrixCellView | undefined>;
}

@Component({
  selector: 'app-computation-formula-page',
  standalone: true,
  imports: [NgIf, NgFor, AsyncPipe, ReactiveFormsModule, RouterLink],
  templateUrl: './computation-formula-page.component.html',
  styleUrl: './computation-formula-page.component.scss'
})
export class ComputationFormulaPageComponent implements OnInit {
  readonly roundingModes: RoundingPolicy[] = ['NONE', 'HALF_UP', 'HALF_EVEN', 'FLOOR', 'CEIL'];

  recipeId: number | null = null;
  recipeName: string | null = null;

  loading$ = new BehaviorSubject<boolean>(false);
  formulas$ = new BehaviorSubject<ComputationFormula[]>([]);
  parameterOptions$ = new BehaviorSubject<ParameterSelectorOption[]>([]);

  matrixColumns: MatrixColumnView[] = [];
  matrixRows: MatrixRowView[] = [];

  selectionMode: 'target' | 'reference' = 'target';
  activeReferenceIndex = 0;

  editingFormulaId: number | null = null;
  private optionByKey = new Map<string, ParameterSelectorOption>();

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private computationFormulaApiService: ComputationFormulaApiService,
    private recipeApiService: RecipeApiService
  ) {
    this.form = this.fb.group({
      targetSelection: [''],
      targetStepCode: ['', [Validators.required]],
      targetDefinitionPath: ['', [Validators.required]],
      expression: ['', [Validators.required]],
      roundingMode: ['HALF_UP' as RoundingPolicy, Validators.required],
      decimals: [null as number | null],
      label: [''],
      references: this.fb.array([])
    });
  }

  ngOnInit(): void {
    const recipeId = Number(this.route.snapshot.paramMap.get('id'));
    if (Number.isNaN(recipeId)) {
      this.router.navigate(['/recipes']);
      return;
    }

    this.recipeId = recipeId;
    this.ensureAtLeastOneReference();
    this.loadRecipeHeader(recipeId);
    this.loadParameterOptions(recipeId);
    this.loadFormulas(recipeId);
  }

  get referencesArray(): FormArray {
    return this.form.get('references') as FormArray;
  }

  addReference(): void {
    this.referencesArray.push(
      this.fb.group({
        id: [null as number | null],
        sourceSelection: [''],
        slot: [this.referencesArray.length + 1, [Validators.required, Validators.min(1)]],
        stepCode: ['', Validators.required],
        definitionPath: ['', Validators.required]
      })
    );
  }

  removeReference(index: number): void {
    if (this.referencesArray.length <= 1) {
      return;
    }

    this.referencesArray.removeAt(index);
    if (this.activeReferenceIndex >= this.referencesArray.length) {
      this.activeReferenceIndex = Math.max(0, this.referencesArray.length - 1);
    }
    this.reindexSlots();
  }

  editFormula(formula: ComputationFormula): void {
    this.editingFormulaId = formula.id ?? null;
    const targetSelection = this.findSelectionKey(formula.targetStepCode, formula.targetDefinitionPath);

    this.form.patchValue({
      targetSelection,
      targetStepCode: formula.targetStepCode,
      targetDefinitionPath: formula.targetDefinitionPath,
      expression: formula.expression,
      roundingMode: formula.roundingMode,
      decimals: formula.decimals ?? null,
      label: formula.label ?? ''
    });

    this.referencesArray.clear();
    const sortedReferences = [...(formula.references ?? [])]
      .sort((a, b) => a.slot - b.slot);

    if (sortedReferences.length === 0) {
      this.addReference();
      return;
    }

    for (const reference of sortedReferences) {
      this.referencesArray.push(
        this.fb.group({
          id: [reference.id ?? null],
          sourceSelection: [this.findSelectionKey(reference.stepCode, reference.definitionPath)],
          slot: [reference.slot, [Validators.required, Validators.min(1)]],
          stepCode: [reference.stepCode, Validators.required],
          definitionPath: [reference.definitionPath, Validators.required]
        })
      );
    }
  }

  cancelEdit(): void {
    this.editingFormulaId = null;
    this.resetForm();
  }

  submit(): void {
    if (!this.recipeId) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: ComputationFormula = {
      recipeId: this.recipeId,
      targetStepCode: raw.targetStepCode?.trim() ?? '',
      targetDefinitionPath: raw.targetDefinitionPath?.trim() ?? '',
      expression: raw.expression?.trim() ?? '',
      roundingMode: raw.roundingMode!,
      decimals: raw.decimals ?? null,
      label: raw.label?.trim() ? raw.label.trim() : null,
      references: ((raw.references ?? []) as Array<{
        id?: number | null;
        slot?: number | string | null;
        stepCode?: string | null;
        definitionPath?: string | null;
      }>).map((reference) => ({
        id: reference.id ?? undefined,
        slot: Number(reference.slot),
        stepCode: reference.stepCode?.trim() ?? '',
        definitionPath: reference.definitionPath?.trim() ?? ''
      }))
    };

    this.loading$.next(true);

    const request$ = this.editingFormulaId
      ? this.computationFormulaApiService.updateFormula(this.editingFormulaId, payload)
      : this.computationFormulaApiService.createFormula(this.recipeId, payload);

    request$.subscribe({
      next: () => {
        this.loading$.next(false);
        this.cancelEdit();
        this.loadFormulas(this.recipeId!);
      },
      error: (error) => {
        console.error('Failed to save computation formula', error);
        this.loading$.next(false);
      }
    });
  }

  deleteFormula(formula: ComputationFormula): void {
    if (!formula.id) {
      return;
    }

    const confirmed = window.confirm(`Delete formula #${formula.id}?`);
    if (!confirmed) {
      return;
    }

    this.loading$.next(true);
    this.computationFormulaApiService.deleteFormula(formula.id).subscribe({
      next: () => {
        this.loading$.next(false);
        if (this.editingFormulaId === formula.id) {
          this.cancelEdit();
        }
        this.loadFormulas(this.recipeId!);
      },
      error: (error) => {
        console.error('Failed to delete computation formula', error);
        this.loading$.next(false);
      }
    });
  }

  private loadFormulas(recipeId: number): void {
    this.loading$.next(true);
    this.computationFormulaApiService.getFormulasByRecipe(recipeId).subscribe({
      next: (formulas) => {
        this.formulas$.next(formulas);
        this.loading$.next(false);
      },
      error: (error) => {
        console.error('Failed to load computation formulas', error);
        this.formulas$.next([]);
        this.loading$.next(false);
      }
    });
  }

  selectTargetParameter(selectionKey: string): void {
    const option = this.parameterOptions$.value.find((candidate) => candidate.key === selectionKey);
    if (!option) {
      this.form.patchValue({
        targetStepCode: '',
        targetDefinitionPath: ''
      });
      return;
    }

    this.form.patchValue({
      targetSelection: option.key,
      targetStepCode: option.stepCode,
      targetDefinitionPath: option.definitionPath
    });
  }

  selectReferenceParameter(index: number, selectionKey: string): void {
    const control = this.referencesArray.at(index);
    if (!control) {
      return;
    }

    const option = this.parameterOptions$.value.find((candidate) => candidate.key === selectionKey);
    if (!option) {
      control.patchValue({
        stepCode: '',
        definitionPath: ''
      });
      return;
    }

    control.patchValue({
      sourceSelection: option.key,
      stepCode: option.stepCode,
      definitionPath: option.definitionPath
    });
  }

  setSelectionMode(mode: 'target' | 'reference', referenceIndex = 0): void {
    this.selectionMode = mode;
    if (mode === 'reference') {
      this.activeReferenceIndex = Math.max(0, referenceIndex);
      this.ensureReferenceIndex(this.activeReferenceIndex);
    }
  }

  pickFromMatrix(cell: MatrixCellView): void {
    if (this.selectionMode === 'target') {
      this.selectTargetParameter(cell.key);
      return;
    }

    this.selectReferenceParameter(this.activeReferenceIndex, cell.key);
  }

  onFormulaBadgeClick(formula: ComputationFormula, cell: MatrixCellView, event: Event): void {
    event.stopPropagation();
    if (this.selectionMode === 'reference') {
      this.pickFromMatrix(cell);
      return;
    }
    this.editFormula(formula);
  }

  isCellSelectedAsReference(cell: MatrixCellView): boolean {
    const stepCode = cell.stepCode;
    const definitionPath = cell.definitionPath;
    return this.referencesArray.controls.some((control) =>
      control.get('stepCode')?.value === stepCode
      && control.get('definitionPath')?.value === definitionPath
    );
  }

  getTargetSelectionLabel(): string {
    const key = (this.form.get('targetSelection')?.value as string) ?? '';
    return key ? this.resolveSelectionLabel(key) : 'Not selected';
  }

  getReferenceSelectionLabel(index: number): string {
    const control = this.referencesArray.at(index);
    const key = (control?.get('sourceSelection')?.value as string) ?? '';
    return key ? this.resolveSelectionLabel(key) : 'Not selected';
  }

  getAddressLabel(stepCode: string, definitionPath: string): string {
    return this.resolveSelectionLabel(this.toSelectionKey(stepCode, definitionPath));
  }

  private loadRecipeHeader(recipeId: number): void {
    this.recipeApiService.getRecipeById(recipeId).subscribe({
      next: (recipe) => {
        this.recipeName = recipe.name;
      },
      error: (error) => {
        console.error('Failed to load recipe header', error);
      }
    });
  }

  private ensureAtLeastOneReference(): void {
    if (this.referencesArray.length === 0) {
      this.addReference();
    }
  }

  private loadParameterOptions(recipeId: number): void {
    this.recipeApiService.getRecipeStepParameterGrid(recipeId).subscribe({
      next: (rows) => {
        const options = this.buildParameterOptions(rows);
        this.parameterOptions$.next(options);
        this.optionByKey = new Map(options.map((option) => [option.key, option]));
        this.buildMatrix(rows);
      },
      error: (error) => {
        console.error('Failed to load parameter options', error);
        this.parameterOptions$.next([]);
        this.optionByKey.clear();
        this.matrixColumns = [];
        this.matrixRows = [];
      }
    });
  }

  private buildParameterOptions(rows: StepParameterGridRow[]): ParameterSelectorOption[] {
    const optionsByKey = new Map<string, ParameterSelectorOption>();

    for (const row of rows) {
      const stepCode = row.stepCode?.trim();
      const definitionPath = row.parameterDefinitionPath?.trim();
      if (!stepCode || !definitionPath) {
        continue;
      }

      const key = this.toSelectionKey(stepCode, definitionPath);
      if (optionsByKey.has(key)) {
        continue;
      }

      const parameterLabel = row.parameterDefinitionName || `Definition ${row.parameterDefinitionId}`;
      const stepLabel = row.stepName ? `${stepCode} (${row.stepName})` : stepCode;

      optionsByKey.set(key, {
        key,
        stepCode,
        definitionPath,
        label: `${stepLabel} - ${parameterLabel} [${definitionPath}]`
      });
    }

    return [...optionsByKey.values()].sort((a, b) => a.label.localeCompare(b.label));
  }

  private buildMatrix(rows: StepParameterGridRow[]): void {
    const columnsByStepId = new Map<number, MatrixColumnView>();
    for (const row of rows) {
      if (!row.stepId) {
        continue;
      }

      if (row.stepKind === 'PRESTEP') {
        continue;
      }

      if (!columnsByStepId.has(row.stepId)) {
        columnsByStepId.set(row.stepId, {
          stepId: row.stepId,
          stepCode: row.stepCode,
          stepName: row.stepName,
          stepOrderIndex: row.stepOrderIndex
        });
      }
    }

    this.matrixColumns = [...columnsByStepId.values()]
      .sort((a, b) => a.stepOrderIndex - b.stepOrderIndex);

    const rowsByKey = new Map<string, MatrixRowView>();
    for (const row of rows) {
      const stepCode = row.stepCode?.trim();
      const definitionPath = row.parameterDefinitionPath?.trim();
      if (!stepCode || !definitionPath || !row.stepId) {
        continue;
      }

      if (row.stepKind === 'PRESTEP') {
        continue;
      }

      const rowKey = `${row.parameterDefinitionId}|${definitionPath}`;
      let matrixRow = rowsByKey.get(rowKey);
      if (!matrixRow) {
        matrixRow = {
          key: rowKey,
          parameterLabel: row.parameterDefinitionName,
          parameterGroup: row.parameterGroup?.trim() || 'Ungrouped',
          parameterGroupOrder: row.parameterGroupOrder ?? Number.MAX_SAFE_INTEGER,
          definitionPath,
          cellsByStepId: {}
        };
        rowsByKey.set(rowKey, matrixRow);
      }

      const cellKey = this.toSelectionKey(stepCode, definitionPath);
      matrixRow.cellsByStepId[row.stepId] = {
        key: cellKey,
        stepCode,
        definitionPath,
        parameterLabel: row.parameterDefinitionName
      };
    }

    this.matrixRows = [...rowsByKey.values()].sort((a, b) => {
      if (a.parameterGroupOrder !== b.parameterGroupOrder) {
        return a.parameterGroupOrder - b.parameterGroupOrder;
      }

      const groupCompare = a.parameterGroup.localeCompare(b.parameterGroup);
      if (groupCompare !== 0) {
        return groupCompare;
      }

      const labelCompare = a.parameterLabel.localeCompare(b.parameterLabel);
      if (labelCompare !== 0) {
        return labelCompare;
      }

      return a.definitionPath.localeCompare(b.definitionPath);
    });
  }

  private resetForm(): void {
    this.form.reset({
      targetSelection: '',
      targetStepCode: '',
      targetDefinitionPath: '',
      expression: '',
      roundingMode: 'HALF_UP',
      decimals: null,
      label: ''
    });

    this.referencesArray.clear();
    this.addReference();
    this.setSelectionMode('target');
  }

  private reindexSlots(): void {
    this.referencesArray.controls.forEach((control, index) => {
      control.get('slot')?.setValue(index + 1);
    });
  }

  private toSelectionKey(stepCode: string, definitionPath: string): string {
    return `${stepCode}|${definitionPath}`;
  }

  private findSelectionKey(stepCode: string, definitionPath: string): string {
    const key = this.toSelectionKey(stepCode, definitionPath);
    const exists = this.parameterOptions$.value.some((candidate) => candidate.key === key);
    return exists ? key : '';
  }

  private ensureReferenceIndex(index: number): void {
    while (this.referencesArray.length <= index) {
      this.addReference();
    }
  }

  private resolveSelectionLabel(selectionKey: string): string {
    const option = this.optionByKey.get(selectionKey);
    if (option) {
      return option.label;
    }

    const separatorIndex = selectionKey.indexOf('|');
    if (separatorIndex < 0) {
      return selectionKey;
    }

    const stepCode = selectionKey.slice(0, separatorIndex);
    const definitionPath = selectionKey.slice(separatorIndex + 1);
    return `${stepCode} [${definitionPath}]`;
  }
}
