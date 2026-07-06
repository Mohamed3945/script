import { Component, Input } from '@angular/core';
import { ChamberDetail } from '../../../../../core/models/chamber-detail.model';

@Component({
  selector: 'app-chamber-summary-card',
  standalone: true,
  templateUrl: './chamber-summary-card.component.html',
  styleUrl: './chamber-summary-card.component.scss'
})
export class ChamberSummaryCardComponent {
  @Input() chamber!: ChamberDetail;
}
