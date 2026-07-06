import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-result-final-actions',
  standalone: true,
  templateUrl: './result-final-actions.component.html',
  styleUrl: './result-final-actions.component.scss'
})
export class ResultFinalActionsComponent {
  @Input() canSubmit = false;
  @Input() loading = false;

  @Output() restartClicked = new EventEmitter<void>();
  @Output() saveAndCustomizeClicked = new EventEmitter<void>();
}
