import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { CompatibleChamber } from '../../../../../core/models/compatible-chamber.model';
import { CompatibleMachine } from '../../../../../core/models/compatible-machine.model';
import { RecipeCompatibilityResult } from '../../../../../core/models/recipe-compatibility-result.model';

@Component({
  selector: 'app-recipe-compatible-machines-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './recipe-compatible-machines-panel.component.html',
  styleUrl: './recipe-compatible-machines-panel.component.scss'
})
export class RecipeCompatibleMachinesPanelComponent {
  @Input() compatibility: RecipeCompatibilityResult | null = null;
  @Input() loading = false;

  trackMachineById = (_: number, item: CompatibleMachine): number => item.machineId;
  trackChamberById = (_: number, item: CompatibleChamber): number => item.chamberId;
}