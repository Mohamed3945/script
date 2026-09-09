import { Routes } from '@angular/router';
import { ComputationFormulaPageComponent } from './pages/computation-formula-page/computation-formula-page.component';
import { RecipeCreatePageComponent } from './pages/recipe-create-page/recipe-create-page.component';
import { RecipeDetailPageComponent } from './pages/recipe-detail-page/recipe-detail-page.component';
import { RecipeEditPageComponent } from './pages/recipe-edit-page/recipe-edit-page.component';
import { RecipeListPageComponent } from './pages/recipe-list-page/recipe-list-page.component';

export const RECIPE_MANAGEMENT_ROUTES: Routes = [
  {
    path: '',
    component: RecipeListPageComponent
  },
  {
    path: 'new',
    component: RecipeCreatePageComponent
  },
  {
    path: ':id/edit',
    component: RecipeEditPageComponent
  },
  {
    path: 'golden/:id/formulas',
    component: ComputationFormulaPageComponent,
    data: { workspaceMode: 'golden' }
  },
  {
    path: 'golden/:id',
    component: RecipeDetailPageComponent,
    data: { workspaceMode: 'golden' }
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
