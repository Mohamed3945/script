import { Component, Input } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { StepParameterGridRow } from '../../../../../core/models/step-parameter-grid-row.model';

@Component({
  selector: 'app-step-parameter-grid',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './step-parameter-grid.component.html',
  styleUrl: './step-parameter-grid.component.scss'
})
export class StepParameterGridComponent {
  @Input() rows: StepParameterGridRow[] = [];
}