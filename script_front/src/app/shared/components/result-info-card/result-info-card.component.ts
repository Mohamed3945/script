import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-result-info-card',
  standalone: true,
  templateUrl: './result-info-card.component.html',
  styleUrl: './result-info-card.component.scss'
})
export class ResultInfoCardComponent {
  @Input() title = '';
  @Input() icon = '';
  @Input() value = '';
  @Input() subValue = '';
  @Input() theme: 'blue' | 'gold' = 'blue';
}