import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Machine } from '../../../../../core/models/machine.model';

@Component({
  selector: 'app-machine-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './machine-table.component.html',
  styleUrl: './machine-table.component.scss'
})
export class MachineTableComponent {
  @Input() machines: Machine[] = [];

  @Output() view = new EventEmitter<Machine>();
  @Output() edit = new EventEmitter<Machine>();
  @Output() delete = new EventEmitter<Machine>();
}
