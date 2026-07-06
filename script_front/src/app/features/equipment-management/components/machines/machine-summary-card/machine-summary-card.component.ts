import { Component, Input } from '@angular/core';
import { MachineDetail } from '../../../../../core/models/machine-detail.model';

@Component({
  selector: 'app-machine-summary-card',
  standalone: true,
  templateUrl: './machine-summary-card.component.html',
  styleUrl: './machine-summary-card.component.scss'
})
export class MachineSummaryCardComponent {
  @Input() machine!: MachineDetail;
}
