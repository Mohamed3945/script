import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ChamberCapability } from '../../../../../core/models/chamber-capability.model';

@Component({
  selector: 'app-chamber-capability-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './chamber-capability-table.component.html',
  styleUrl: './chamber-capability-table.component.scss'
})
export class ChamberCapabilityTableComponent {
  @Input() capabilities: ChamberCapability[] = [];
  @Input() mode: 'catalog' | 'assigned' = 'catalog';

  @Output() edit = new EventEmitter<ChamberCapability>();
  @Output() delete = new EventEmitter<ChamberCapability>();
  @Output() remove = new EventEmitter<ChamberCapability>();
}
