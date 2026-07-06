import { Routes } from '@angular/router';

export const EQUIPMENT_MANAGEMENT_ROUTES: Routes = [
  {
    path: 'machines',
    loadComponent: () => import('./pages/machine-list-page/machine-list-page.component').then(m => m.MachineListPageComponent)
  },
  {
    path: 'machines/new',
    loadComponent: () => import('./pages/machine-create-page/machine-create-page.component').then(m => m.MachineCreatePageComponent)
  },
  {
    path: 'machines/:id',
    loadComponent: () => import('./pages/machine-detail-page/machine-detail-page.component').then(m => m.MachineDetailPageComponent)
  },
  {
    path: 'machines/:id/edit',
    loadComponent: () => import('./pages/machine-edit-page/machine-edit-page.component').then(m => m.MachineEditPageComponent)
  },
  {
    path: 'machines/:machineId/chambers/new',
    loadComponent: () => import('./pages/chamber-create-page/chamber-create-page.component').then(m => m.ChamberCreatePageComponent)
  },
  {
    path: 'chambers/:id',
    loadComponent: () => import('./pages/chamber-detail-page/chamber-detail-page.component').then(m => m.ChamberDetailPageComponent)
  },
  {
    path: 'chambers/:id/edit',
    loadComponent: () => import('./pages/chamber-edit-page/chamber-edit-page.component').then(m => m.ChamberEditPageComponent)
  },
  {
    path: 'chamber-capabilities',
    loadComponent: () => import('./pages/chamber-capability-list-page/chamber-capability-list-page.component').then(m => m.ChamberCapabilityListPageComponent)
  },
  {
    path: 'chamber-capabilities/new',
    loadComponent: () => import('./pages/chamber-capability-create-page/chamber-capability-create-page.component').then(m => m.ChamberCapabilityCreatePageComponent)
  },
  {
    path: 'chamber-capabilities/:id/edit',
    loadComponent: () => import('./pages/chamber-capability-edit-page/chamber-capability-edit-page.component').then(m => m.ChamberCapabilityEditPageComponent)
  },
  {
    path: 'configuration-definitions',
    loadComponent: () => import('./pages/configuration-definition-list-page/configuration-definition-list-page.component').then(m => m.ConfigurationDefinitionListPageComponent)
  },
  {
    path: 'configuration-definitions/new',
    loadComponent: () => import('./pages/configuration-definition-create-page/configuration-definition-create-page.component').then(m => m.ConfigurationDefinitionCreatePageComponent)
  },
  {
    path: 'configuration-definitions/:id',
    loadComponent: () => import('./pages/configuration-definition-detail-page/configuration-definition-detail-page.component').then(m => m.ConfigurationDefinitionDetailPageComponent)
  },
  {
    path: 'configuration-definitions/:id/edit',
    loadComponent: () => import('./pages/configuration-definition-edit-page/configuration-definition-edit-page.component').then(m => m.ConfigurationDefinitionEditPageComponent)
  }
];