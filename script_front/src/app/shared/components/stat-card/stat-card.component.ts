import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [NgClass],
  templateUrl: './stat-card.component.html',
  styleUrl: './stat-card.component.scss'
})
export class StatCardComponent {
  @Input() icon = '';
  @Input() value = '';
  @Input() label = '';
  @Input() variant: 'blue' | 'green' | 'gold' = 'blue';
}