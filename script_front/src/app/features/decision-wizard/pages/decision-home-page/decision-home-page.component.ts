import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { StatCardComponent } from '../../../../shared/components/stat-card/stat-card.component';
import { WizardStartCardComponent } from '../../components/wizard-start-card/wizard-start-card.component';
import { NgFor } from '@angular/common';

@Component({
  selector: 'app-decision-home-page',
  standalone: true,
  imports: [NgFor, StatCardComponent, WizardStartCardComponent],
  templateUrl: './decision-home-page.component.html',
  styleUrl: './decision-home-page.component.scss'
})
export class DecisionHomePageComponent {
  stats = [
    { icon: 'ti ti-file-check', value: '4', label: 'Golden recipes available', variant: 'blue' as const },
    { icon: 'ti ti-file-check', value: '7', label: 'Recipes created with golden ', variant: 'blue' as const },
    { icon: 'ti ti-cpu', value: '12', label: 'Compatible machines indexed', variant: 'green' as const },
    { icon: 'ti ti-trending-up', value: '94%', label: 'First-match accuracy rate', variant: 'gold' as const }
  ];

  constructor(private router: Router) {}

  startWizard(): void {
    this.router.navigate(['/wizard']);
  }
}