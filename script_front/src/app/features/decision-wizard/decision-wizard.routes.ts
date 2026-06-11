import { Routes } from '@angular/router';
import { DecisionHomePageComponent } from './pages/decision-home-page/decision-home-page.component';
import { DecisionWizardPageComponent } from './pages/decision-wizard-page/decision-wizard-page.component';
import { DecisionResultPageComponent } from './pages/decision-result-page/decision-result-page.component';

export const DECISION_WIZARD_ROUTES: Routes = [
  { path: '', component: DecisionHomePageComponent },
  { path: 'wizard', component: DecisionWizardPageComponent },
  { path: 'result', component: DecisionResultPageComponent }
];