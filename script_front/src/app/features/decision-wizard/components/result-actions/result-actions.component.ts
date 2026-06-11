import { Component, EventEmitter, Output } from '@angular/core';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button.component';

@Component({
  selector: 'app-result-actions',
  standalone: true,
  imports: [PrimaryButtonComponent],
  templateUrl: './result-actions.component.html',
  styleUrl: './result-actions.component.scss'
})
export class ResultActionsComponent {
  @Output() restartClicked = new EventEmitter<void>();
  @Output() saveClicked = new EventEmitter<void>();
}