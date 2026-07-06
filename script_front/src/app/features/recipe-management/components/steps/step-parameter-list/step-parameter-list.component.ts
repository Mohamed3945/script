import { Component, Input } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { StepParameter } from '../../../../../core/models/step-parameter.model';

@Component({
  selector: 'app-step-parameter-list',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './step-parameter-list.component.html',
  styleUrl: './step-parameter-list.component.scss'
})
/**
 * StepParameterListComponent coordinates UI logic for this feature.
 */
export class StepParameterListComponent {
  @Input() parameters: StepParameter[] = [];
}
