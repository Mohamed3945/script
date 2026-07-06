import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor } from '@angular/common';
import { MachineChoice } from '../../../../core/models/machine-choice.model';

@Component({
  selector: 'app-result-machine-selection-card',
  standalone: true,
  imports: [NgFor],
  templateUrl: './result-machine-selection-card.component.html',
  styleUrl: './result-machine-selection-card.component.scss'
})
export class ResultMachineSelectionCardComponent {
  @Input() machines: MachineChoice[] = [];
  @Input() selectedMachineId: number | null = null;

  @Output() machineSelected = new EventEmitter<number>();

  selectMachine(machineId: number): void {
    this.machineSelected.emit(machineId);
  }
}
