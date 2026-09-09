import { Routes } from '@angular/router';
import { ComputationFormulaPageComponent } from './pages/computation-formula-page/computation-formula-page.component';
import { RecipeCreatePageComponent } from './pages/recipe-create-page/recipe-create-page.component';
import { RecipeDetailPageComponent } from './pages/recipe-detail-page/recipe-detail-page.component';
import { RecipeEditPageComponent } from './pages/recipe-edit-page/recipe-edit-page.component';
import { RecipeListPageComponent } from './pages/recipe-list-page/recipe-list-page.component';
import { roleGuard } from '../../core/guards/role.guard';

export const RECIPE_MANAGEMENT_ROUTES: Routes = [
  {
    path: '',
    component: RecipeListPageComponent
  },
  {
    path: 'new',
    canActivate: [roleGuard],
    data: { roles: ['SUPER'] },
    component: RecipeCreatePageComponent
  },
  {
    path: ':id/edit',
    component: RecipeEditPageComponent
  },
  {
    path: 'golden/:id/formulas',
    canActivate: [roleGuard],
    data: { workspaceMode: 'golden', roles: ['SUPER'] },
    component: ComputationFormulaPageComponent,
  },
  {
    path: 'golden/:id',
    canActivate: [roleGuard],
    data: { workspaceMode: 'golden', roles: ['SUPER'] },
    component: RecipeDetailPageComponent,
  },
  {
    path: 'derived/:id',
    component: RecipeDetailPageComponent,
    data: { workspaceMode: 'derived' }
  },
  {
    path: ':id',
    component: RecipeDetailPageComponent,
    data: { workspaceMode: 'auto' }
  }
];
