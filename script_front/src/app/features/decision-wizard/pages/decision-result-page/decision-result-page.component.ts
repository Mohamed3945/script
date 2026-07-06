import { AsyncPipe, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { MachineChoice } from '../../../../core/models/machine-choice.model';
import { DecisionFinalizeRequest } from '../../../../core/models/decision-finalize-request.model';
import { DecisionWizardService } from '../../../../core/services/decision-wizard.service';
import { DecisionFinalizeApiService } from '../../../../core/services/decision-finalize-api.service';
import { buildRecipeDetailRouteByKind } from '../../../../core/utils/recipe-route.util';
import { ResultSummaryComponent } from '../../components/result-summary/result-summary.component';
import { ResultInfoCardComponent } from '../../../../shared/components/result-info-card/result-info-card.component';
import { ResultGoldenSelectionCardComponent } from '../../components/result-golden-selection-card/result-golden-selection-card.component';
import { ResultFinalActionsComponent } from '../../components/result-final-actions/result-final-actions.component';

@Component({
  selector: 'app-decision-result-page',
  standalone: true,
  imports: [
    NgIf,
    AsyncPipe,
    ResultSummaryComponent,
    ResultInfoCardComponent,
    ResultGoldenSelectionCardComponent,
    ResultFinalActionsComponent
  ],
  templateUrl: './decision-result-page.component.html',
  styleUrl: './decision-result-page.component.scss'
})
export class DecisionResultPageComponent {
  private readonly wizardService = inject(DecisionWizardService);
  private readonly decisionFinalizeApiService = inject(DecisionFinalizeApiService);
  private readonly router = inject(Router);

  result$ = this.wizardService.result$;
  selections$ = this.wizardService.selections$;

  loading$ = new BehaviorSubject<boolean>(false);

  goldenValidated = false;
  machineChoices: MachineChoice[] = [];

  constructor() {
    const result = this.wizardService.resultSnapshot ?? null;
    if (result?.machineId) {
      this.machineChoices = [
        {
          id: result.machineId,
          code: `MACHINE_${result.machineId}`,
          label: `Compatible machine #${result.machineId}`,
          selected: false
        }
      ];
    } else {
      this.machineChoices = [];
    }
  }

  get canSubmit(): boolean {
    return this.goldenValidated && Boolean(this.wizardService.resultSnapshot?.machineId);
  }

  onGoldenValidatedChange(value: boolean): void {
    this.goldenValidated = value;
  }

  restart(): void {
    this.wizardService.reset();
    this.router.navigate(['/wizard']);
  }

  saveAndCustomize(): void {
    const result = this.wizardService.resultSnapshot ?? null;

    if (!result?.id || !result.goldenRecipeId || !result.machineId || !this.goldenValidated) {
      return;
    }

    const payload: DecisionFinalizeRequest = {
      resultProfileId: result.id,
      validatedGoldenRecipeId: result.goldenRecipeId,
      selectedMachineId: result.machineId,
      creatorId: 1,
      answers: this.wizardService.buildExecutionAnswers()
    };

    this.loading$.next(true);

    this.decisionFinalizeApiService.finalizeDecision(payload).subscribe({
      next: (response) => {
        this.loading$.next(false);
        this.router.navigate(buildRecipeDetailRouteByKind(response.derivedRecipeId, 'DERIVED'), {
          state: { compatibleMachines: this.machineChoices }
        });
      },
      error: (error) => {
        console.error('Failed to finalize decision and create derived recipe', error);
        this.loading$.next(false);
      }
    });
  }
}