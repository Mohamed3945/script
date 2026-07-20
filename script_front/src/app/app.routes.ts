import { Routes } from '@angular/router';
import { DECISION_WIZARD_ROUTES } from './features/decision-wizard/decision-wizard.routes';
import { EQUIPMENT_MANAGEMENT_ROUTES } from './features/equipment-management/equipment-management.routes';
import { PARAMETER_MANAGEMENT_ROUTES } from './features/parameter-management/parameter-management.routes';
import { RECIPE_MANAGEMENT_ROUTES } from './features/recipe-management/recipe-management.routes';
import { REFERENCE_DATA_MANAGEMENT_ROUTES } from './features/reference-data-management/reference-data-management.routes';
import { RULES_MANAGEMENT_ROUTES } from './features/rules-management/rules-management.routes';

export const routes: Routes = [
  {
    path: '',
    children: DECISION_WIZARD_ROUTES
  },
  {
    path: 'parameter',
    redirectTo: '/parameters',
    pathMatch: 'full'
  },
  {
    path: 'parameter/new',
    redirectTo: '/parameters/new',
    pathMatch: 'full'
  },
  {
    path: 'parameter/:id',
    redirectTo: '/parameters/:id',
    pathMatch: 'full'
  },
  {
    path: 'parameter/:id/edit',
    redirectTo: '/parameters/:id/edit',
    pathMatch: 'full'
  },
  {
    path: 'recipes/parameter',
    redirectTo: '/parameters',
    pathMatch: 'full'
  },
  {
    path: 'recipes/parameter/new',
    redirectTo: '/parameters/new',
    pathMatch: 'full'
  },
  {
    path: 'recipes/parameter/:id',
    redirectTo: '/parameters/:id',
    pathMatch: 'full'
  },
  {
    path: 'recipes/parameter/:id/edit',
    redirectTo: '/parameters/:id/edit',
    pathMatch: 'full'
  },
  {
    path: 'recipes/parameter-definitions',
    redirectTo: '/parameters',
    pathMatch: 'full'
  },
  {
    path: 'recipes/parameter-definitions/:id',
    redirectTo: '/parameters/:id',
    pathMatch: 'full'
  },
  {
    path: 'recipes/parameter-definitions/:id/edit',
    redirectTo: '/parameters/:id/edit',
    pathMatch: 'full'
  },
  {
    path: 'recipes/rules',
    redirectTo: '/rules',
    pathMatch: 'full'
  },
  {
    path: 'recipes/rules/new',
    redirectTo: '/rules/new',
    pathMatch: 'full'
  },
  {
    path: 'recipes/rules/:id',
    redirectTo: '/rules/:id',
    pathMatch: 'full'
  },
  {
    path: 'recipes/rules/:id/edit',
    redirectTo: '/rules/:id/edit',
    pathMatch: 'full'
  },
  {
    path: 'chamber-capabilities',
    redirectTo: '/reference-data/capabilities',
    pathMatch: 'full'
  },
  {
    path: 'chamber-capabilities/new',
    redirectTo: '/reference-data/capabilities/new',
    pathMatch: 'full'
  },
  {
    path: 'chamber-capabilities/:id',
    redirectTo: '/reference-data/capabilities/:id/edit',
    pathMatch: 'full'
  },
  {
    path: 'chamber-capabilities/:id/edit',
    redirectTo: '/reference-data/capabilities/:id/edit',
    pathMatch: 'full'
  },
  {
    path: 'configuration-definitions',
    redirectTo: '/reference-data/configuration-definitions',
    pathMatch: 'full'
  },
  {
    path: 'configuration-definitions/new',
    redirectTo: '/reference-data/configuration-definitions/new',
    pathMatch: 'full'
  },
  {
    path: 'configuration-definitions/:id',
    redirectTo: '/reference-data/configuration-definitions/:id',
    pathMatch: 'full'
  },
  {
    path: 'configuration-definitions/:id/edit',
    redirectTo: '/reference-data/configuration-definitions/:id/edit',
    pathMatch: 'full'
  },
  {
    path: 'recipes',
    children: RECIPE_MANAGEMENT_ROUTES
  },
  {
    path: 'parameters',
    children: PARAMETER_MANAGEMENT_ROUTES
  },
  {
    path: 'rules',
    children: RULES_MANAGEMENT_ROUTES
  },
  {
    path: 'reference-data',
    children: REFERENCE_DATA_MANAGEMENT_ROUTES
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