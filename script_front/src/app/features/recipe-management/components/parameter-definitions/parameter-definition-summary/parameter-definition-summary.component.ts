import { Component, Input } from '@angular/core';
import { ParameterDefinitionDetail } from '../../../../../core/models/parameter-definition-detail.model';

@Component({
  selector: 'app-parameter-definition-summary',
  standalone: true,
  templateUrl: './parameter-definition-summary.component.html',
  styleUrl: './parameter-definition-summary.component.scss'
})
export class ParameterDefinitionSummaryComponent {
  @Input() definition!: ParameterDefinitionDetail;
}

