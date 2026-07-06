import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgIf } from '@angular/common';
import { DecisionQuestion } from '../../../../core/models/decision-question.model';
import { OptionListComponent } from '../option-list/option-list.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button.component';

@Component({
  selector: 'app-question-card',
  standalone: true,
  imports: [NgIf, OptionListComponent, PrimaryButtonComponent],
  templateUrl: './question-card.component.html',
  styleUrl: './question-card.component.scss'
})
export class QuestionCardComponent {
  @Input() question!: DecisionQuestion;
  @Input() selectedOptionId: number | null = null;
  @Input() showPrevious = false;

  @Output() optionSelected = new EventEmitter<number>();
  @Output() nextClicked = new EventEmitter<void>();
  @Output() previousClicked = new EventEmitter<void>();
}