import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DecisionOption } from '../../../../core/models/decision-option.model';

@Component({
  selector: 'app-option-item',
  standalone: true,
  templateUrl: './option-item.component.html',
  styleUrl: './option-item.component.scss'
})
export class OptionItemComponent {
  @Input() option!: DecisionOption;
  @Input() selected = false;

  @Output() selectedChange = new EventEmitter<void>();
}