import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-result-golden-selection-card',
  standalone: true,
  templateUrl: './result-golden-selection-card.component.html',
  styleUrl: './result-golden-selection-card.component.scss'
})
export class ResultGoldenSelectionCardComponent {
  @Input() goldenRecipeId?: number | null;
  @Input() goldenCode = '';
  @Input() selected = false;

  @Output() selectedChange = new EventEmitter<boolean>();

  onToggle(): void {
    this.selectedChange.emit(!this.selected);
  }
}
