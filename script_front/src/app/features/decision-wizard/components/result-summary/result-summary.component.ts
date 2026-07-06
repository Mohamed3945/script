import { Component, Input } from '@angular/core';
import { NgFor } from '@angular/common';
import { WizardSelection } from '../../../../core/models/wizard-selection.model';

@Component({
  selector: 'app-result-summary',
  standalone: true,
  imports: [NgFor],
  templateUrl: './result-summary.component.html',
  styleUrl: './result-summary.component.scss'
})
export class ResultSummaryComponent {
  @Input() selections: WizardSelection[] = [];
}