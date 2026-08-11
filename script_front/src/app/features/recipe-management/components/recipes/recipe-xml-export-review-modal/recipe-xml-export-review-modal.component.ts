import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ChamberDetail } from '../../../../../core/models/chamber-detail.model';
import { CompatibleMachine } from '../../../../../core/models/compatible-machine.model';
import { RecipeCompatibilityResult } from '../../../../../core/models/recipe-compatibility-result.model';
import { RecipeRequirements } from '../../../../../core/models/recipe-requirements.model';

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
