import { Routes } from '@angular/router';
import { DECISION_WIZARD_ROUTES } from './features/decision-wizard/decision-wizard.routes';
import { EQUIPMENT_MANAGEMENT_ROUTES } from './features/equipment-management/equipment-management.routes';
import { RECIPE_MANAGEMENT_ROUTES } from './features/recipe-management/recipe-management.routes';

export const routes: Routes = [
  {
    path: '',
    children: DECISION_WIZARD_ROUTES
  },
  {
    path: 'parameter',
    redirectTo: '/recipes/parameter',
    pathMatch: 'full'
  },
  {
    path: 'parameter/new',
    redirectTo: '/recipes/parameter/new',
    pathMatch: 'full'
  },
  {
    path: 'parameter/:id',
    redirectTo: '/recipes/parameter/:id',
    pathMatch: 'full'
  },
  {
    path: 'parameter/:id/edit',
    redirectTo: '/recipes/parameter/:id/edit',
    pathMatch: 'full'
  },
  {
    path: 'rules',
    redirectTo: '/recipes/rules',
    pathMatch: 'full'
  },
  {
    path: 'rules/new',
    redirectTo: '/recipes/rules/new',
    pathMatch: 'full'
  },
  {
    path: 'rules/:id',
    redirectTo: '/recipes/rules/:id',
    pathMatch: 'full'
  },
  {
    path: 'rules/:id/edit',
    redirectTo: '/recipes/rules/:id/edit',
    pathMatch: 'full'
  },
  {
    path: 'recipes',
    children: RECIPE_MANAGEMENT_ROUTES
  },
  {
    path: '',
    children: EQUIPMENT_MANAGEMENT_ROUTES
  },
  {
    path: '**',
    redirectTo: '/',
    pathMatch: 'full'
  }
];