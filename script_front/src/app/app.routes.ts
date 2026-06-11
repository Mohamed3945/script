import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('./features/decision-wizard/decision-wizard.routes')
        .then(m => m.DECISION_WIZARD_ROUTES)
  }
];