import { Component, Input } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { WizardSelection } from '../../../core/models/wizard-selection.model';

@Component({
  selector: 'app-recap-list',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './recap-list.component.html',
  styleUrl: './recap-list.component.scss'
})
export class RecapListComponent {
  @Input() selections: WizardSelection[] = [];
}