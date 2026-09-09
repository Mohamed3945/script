import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MachineApiService } from '../../../../core/services/machine-api.service';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';
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
export class DecisionHomePageComponent implements OnInit {
  stats = [
    { icon: 'ti ti-file-check', value: '-', label: 'Golden recipes available', variant: 'blue' as const },
    { icon: 'ti ti-cpu', value: '-', label: 'Compatible machines indexed', variant: 'green' as const },
    { icon: 'ti ti-trending-up', value: '94%', label: 'First-match accuracy rate', variant: 'gold' as const }
  ];

  constructor(
    private router: Router,
    private recipeApiService: RecipeApiService,
    private machineApiService: MachineApiService
  ) {}

  ngOnInit(): void {
    forkJoin({
      goldenRecipes: this.recipeApiService.getRecipes({ recipeKind: 'GOLDEN' }),
      machines: this.machineApiService.getMachines()
    }).subscribe({
      next: ({ goldenRecipes, machines }) => {
        this.stats[0].value = String(goldenRecipes.length);
        this.stats[1].value = String(machines.length);
      },
      error: (error) => {
        console.error('Failed to load home statistics', error);
        this.stats[0].value = 'N/A';
        this.stats[1].value = 'N/A';
      }
    });
  }

  startWizard(): void {
    this.router.navigate(['/wizard']);
  }
}