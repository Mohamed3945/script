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
  }
];