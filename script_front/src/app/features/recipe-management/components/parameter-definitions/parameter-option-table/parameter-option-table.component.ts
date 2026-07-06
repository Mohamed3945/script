import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ParameterOption } from '../../../../../core/models/parameter-option.model';

@Component({
  selector: 'app-parameter-option-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './parameter-option-table.component.html',
  styleUrl: './parameter-option-table.component.scss'
})
/**
 * ParameterOptionTableComponent coordinates UI logic for this feature.
 */
export class ParameterOptionTableComponent {
  @Input() options: ParameterOption[] = [];

  @Output() editOption = new EventEmitter<ParameterOption>();
  @Output() deleteOption = new EventEmitter<ParameterOption>();
}

