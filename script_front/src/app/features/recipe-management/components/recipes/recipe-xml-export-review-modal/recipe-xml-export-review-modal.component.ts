import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ChamberDetail } from '../../../../../core/models/chamber-detail.model';
import { CompatibleMachine } from '../../../../../core/models/compatible-machine.model';
import { RecipeCustomizationSummary } from '../../../../../core/models/recipe-customization-summary.model';
import { RecipeCompatibilityResult } from '../../../../../core/models/recipe-compatibility-result.model';
import { RecipeRequirements } from '../../../../../core/models/recipe-requirements.model';
import { RecipeUntouchedModifiableItem } from '../../../../../core/models/recipe-untouched-modifiable-item.model';

interface UntouchedStepGroup {
  stepId: number;
  stepCode: string | null;
  stepName: string | null;
  stepKind: string | null;
  items: RecipeUntouchedModifiableItem[];
}

@Component({
  selector: 'app-recipe-xml-export-review-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './recipe-xml-export-review-modal.component.html',
  styleUrl: './recipe-xml-export-review-modal.component.scss'
})
export class RecipeXmlExportReviewModalComponent {
  @Input() visible = false;
  @Input() recipeName: string | null = null;
  @Input() requirements: RecipeRequirements | null = null;
  @Input() customizationSummary: RecipeCustomizationSummary | null = null;
  @Input() loadingCustomizationSummary = false;
  @Input() compatibility: RecipeCompatibilityResult | null = null;
  @Input() selectedMachineId: number | null = null;
  @Input() selectedChamberId: number | null = null;
  @Input() selectedChamberDetail: ChamberDetail | null = null;
  @Input() loadingChamberDetail = false;
  @Input() understandChecked = false;
  @Input() downloading = false;

  @Output() closed = new EventEmitter<void>();
  @Output() machineChanged = new EventEmitter<number | null>();
  @Output() chamberChanged = new EventEmitter<number | null>();
  @Output() understandChanged = new EventEmitter<boolean>();
  @Output() downloadRequested = new EventEmitter<void>();

  get machines(): CompatibleMachine[] {
    return this.compatibility?.machines ?? [];
  }

  get selectedMachine(): CompatibleMachine | null {
    if (this.selectedMachineId == null) {
      return null;
    }

    return this.machines.find((machine) => machine.machineId === this.selectedMachineId) ?? null;
  }

  get canDownload(): boolean {
    return this.understandChecked && !this.downloading;
  }

  get customizationRatePercent(): number {
    const rate = this.customizationSummary?.stats?.customizationRate ?? 0;
    return Math.round(rate * 100);
  }

  get untouchedCount(): number {
    return this.customizationSummary?.stats?.untouchedCount ?? 0;
  }

  get touchedCount(): number {
    return this.customizationSummary?.stats?.touchedCount ?? 0;
  }

  get modifiableCount(): number {
    return this.customizationSummary?.stats?.modifiableCount ?? 0;
  }

  get computedExcludedCount(): number {
    return this.customizationSummary?.stats?.computedExcludedCount ?? 0;
  }

  get untouchedRatePercent(): number {
    if (this.modifiableCount <= 0) {
      return 0;
    }

    return Math.round((this.untouchedCount / this.modifiableCount) * 100);
  }

  get customizationSeverityClass(): string {
    if (this.modifiableCount <= 0) {
      return 'severity-neutral';
    }

    if (this.customizationRatePercent >= 70) {
      return 'severity-good';
    }

    if (this.customizationRatePercent >= 40) {
      return 'severity-warning';
    }

    return 'severity-critical';
  }

  get strictPolicyMessage(): string {
    if (this.untouchedCount <= 0) {
      return 'Ready for strict export policy: no untouched modifiable SP.';
    }

    return `Strict export policy risk: ${this.untouchedCount} untouched modifiable SP detected.`;
  }

  get groupedUntouchedModifiableItems(): UntouchedStepGroup[] {
    const items = this.customizationSummary?.untouchedModifiableItems ?? [];
    const groups = new Map<number, UntouchedStepGroup>();

    for (const item of items) {
      const existing = groups.get(item.stepId);
      if (existing) {
        existing.items.push(item);
        continue;
      }

      groups.set(item.stepId, {
        stepId: item.stepId,
        stepCode: item.stepCode,
        stepName: item.stepName,
        stepKind: item.stepKind,
        items: [item]
      });
    }

    return Array.from(groups.values());
  }

  onMachineSelect(rawValue: string): void {
    const value = rawValue.trim();
    this.machineChanged.emit(value ? Number(value) : null);
  }

  onChamberSelect(rawValue: string): void {
    const value = rawValue.trim();
    this.chamberChanged.emit(value ? Number(value) : null);
  }

  onUnderstandToggle(checked: boolean): void {
    this.understandChanged.emit(checked);
  }
}
