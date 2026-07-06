import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { map } from 'rxjs';
import { ProgressBarComponent } from '../../../../shared/components/progress-bar/progress-bar.component';
import { LoadingPanelComponent } from '../../../../shared/components/loading-panel/loading-panel.component';
import { QuestionCardComponent } from '../../components/question-card/question-card.component';
import { WizardSidebarComponent } from '../../components/wizard-sidebar/wizard-sidebar.component';
import { DecisionWizardService } from '../../../../core/services/decision-wizard.service';

@Component({
  selector: 'app-decision-wizard-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, ProgressBarComponent, LoadingPanelComponent, QuestionCardComponent, WizardSidebarComponent],
  templateUrl: './decision-wizard-page.component.html',
  styleUrl: './decision-wizard-page.component.scss'
})
export class DecisionWizardPageComponent implements OnInit {
  private readonly wizardService = inject(DecisionWizardService);
  private readonly router = inject(Router);

  currentQuestion$ = this.wizardService.currentQuestion$;
  selections$ = this.wizardService.selections$;
  loading$ = this.wizardService.loading$;
  selectedOptionId$ = this.wizardService.selectedOptionId$;

  progress$ = this.selections$.pipe(
    map(selections => Math.min(12 + selections.length * 20, 88))
  );

  step$ = this.selections$.pipe(
    map(selections => selections.length + 1)
  );

  ngOnInit(): void {
    this.wizardService.reset();
    this.wizardService.loadEntryPoint();
  }

  onBackHome(): void {
    this.router.navigate(['/']);
  }

  onSelectOption(optionId: number): void {
    this.wizardService.selectOption(optionId);
  }

  onNext(): void {
    this.wizardService.goNext(
      () => {},
      () => this.router.navigate(['/result'])
    );
  }

  onPrevious(): void {
    this.wizardService.goBack();
  }
}