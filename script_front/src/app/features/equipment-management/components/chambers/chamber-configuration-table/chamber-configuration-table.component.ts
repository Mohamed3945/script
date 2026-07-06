import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ChamberConfiguration } from '../../../../../core/models/chamber-configuration.model';

@Component({
  selector: 'app-chamber-configuration-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './chamber-configuration-table.component.html',
  styleUrl: './chamber-configuration-table.component.scss'
})
export class ChamberConfigurationTableComponent {
  @Input() configurations: ChamberConfiguration[] = [];

  @Output() edit = new EventEmitter<ChamberConfiguration>();
  @Output() delete = new EventEmitter<ChamberConfiguration>();
}
