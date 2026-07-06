import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ChamberCapability } from '../../../../../core/models/chamber-capability.model';

@Component({
  selector: 'app-capability-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './capability-table.component.html',
  styleUrl: './capability-table.component.scss'
})
export class CapabilityTableComponent {
  @Input() capabilities: ChamberCapability[] = [];
  @Output() edit = new EventEmitter<ChamberCapability>();
  @Output() delete = new EventEmitter<ChamberCapability>();
}
