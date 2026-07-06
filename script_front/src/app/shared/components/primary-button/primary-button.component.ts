import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-primary-button',
  standalone: true,
  imports: [NgIf],
  templateUrl: './primary-button.component.html',
  styleUrl: './primary-button.component.scss'
})
export class PrimaryButtonComponent {
  @Input() label = '';
  @Input() icon = '';
  @Input() disabled = false;
  @Input() variant: 'primary' | 'secondary' = 'primary';

  @Output() clicked = new EventEmitter<void>();

  onClick(): void {
    if (!this.disabled) {
      this.clicked.emit();
    }
  }
}