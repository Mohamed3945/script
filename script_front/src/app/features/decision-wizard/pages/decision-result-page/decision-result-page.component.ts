import { AsyncPipe, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { DecisionWizardService } from '../../../../core/services/decision-wizard.service';
import { ResultSummaryComponent } from '../../components/result-summary/result-summary.component';
import { ResultActionsComponent } from '../../components/result-actions/result-actions.component';
import { ResultInfoCardComponent } from '../../../../shared/components/result-info-card/result-info-card.component';

@Component({
  selector: 'app-decision-result-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, ResultSummaryComponent, ResultActionsComponent, ResultInfoCardComponent],
  templateUrl: './decision-result-page.component.html',
  styleUrl: './decision-result-page.component.scss'
})
export class DecisionResultPageComponent {
  private readonly wizardService = inject(DecisionWizardService);
  private readonly router = inject(Router);

  result$ = this.wizardService.result$;
  selections$ = this.wizardService.selections$;

  restart(): void {
    this.wizardService.reset();
    this.router.navigate(['/wizard']);
  }

  save(): void {
    console.log('TODO save configuration');
  }
}