import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor } from '@angular/common';
import { DecisionOption } from '../../../../core/models/decision-option.model';
import { OptionItemComponent } from '../option-item/option-item.component';

@Component({
  selector: 'app-option-list',
  standalone: true,
  imports: [NgFor, OptionItemComponent],
  templateUrl: './option-list.component.html',
  styleUrl: './option-list.component.scss'
})
export class OptionListComponent {
  @Input() options: DecisionOption[] = [];
  @Input() selectedOptionId: number | null = null;
  @Output() optionSelected = new EventEmitter<number>();

  get sortedOptions(): DecisionOption[] {
    return [...this.options].sort((a, b) => a.orderIndex - b.orderIndex);
  }
}