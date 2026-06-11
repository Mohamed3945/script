import { Component, Input } from '@angular/core';
import { RecapListComponent } from '../../../../shared/components/recap-list/recap-list.component';
import { WizardSelection } from '../../../../core/models/wizard-selection.model';

@Component({
  selector: 'app-wizard-sidebar',
  standalone: true,
  imports: [RecapListComponent],
  templateUrl: './wizard-sidebar.component.html',
  styleUrl: './wizard-sidebar.component.scss'
})
export class WizardSidebarComponent {
  @Input() selections: WizardSelection[] = [];
}