import { Component, EventEmitter, Output } from '@angular/core';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button.component';

@Component({
  selector: 'app-wizard-start-card',
  standalone: true,
  imports: [PrimaryButtonComponent],
  templateUrl: './wizard-start-card.component.html',
  styleUrl: './wizard-start-card.component.scss'
})
export class WizardStartCardComponent {
  @Output() start = new EventEmitter<void>();
}