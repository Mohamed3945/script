import { Routes } from '@angular/router';
import { DependencyRuleCreatePageComponent } from './pages/dependency-rule-create-page/dependency-rule-create-page.component';
import { DependencyRuleDetailPageComponent } from './pages/dependency-rule-detail-page/dependency-rule-detail-page.component';
import { DependencyRuleEditPageComponent } from './pages/dependency-rule-edit-page/dependency-rule-edit-page.component';
import { DependencyRuleListPageComponent } from './pages/dependency-rule-list-page/dependency-rule-list-page.component';
import { ParameterDefinitionCreatePageComponent } from './pages/parameter-definition-create-page/parameter-definition-create-page.component';
import { ParameterDefinitionDetailPageComponent } from './pages/parameter-definition-detail-page/parameter-definition-detail-page.component';
import { ParameterDefinitionEditPageComponent } from './pages/parameter-definition-edit-page/parameter-definition-edit-page.component';
import { ParameterDefinitionListPageComponent } from './pages/parameter-definition-list-page/parameter-definition-list-page.component';
import { RecipeCreatePageComponent } from './pages/recipe-create-page/recipe-create-page.component';
import { RecipeDerivedCreatePageComponent } from './pages/recipe-derived-create-page/recipe-derived-create-page.component';
import { RecipeDetailPageComponent } from './pages/recipe-detail-page/recipe-detail-page.component';
import { RecipeEditPageComponent } from './pages/recipe-edit-page/recipe-edit-page.component';
import { RecipeListPageComponent } from './pages/recipe-list-page/recipe-list-page.component';

export const RECIPE_MANAGEMENT_ROUTES: Routes = [
  {
    path: '',
    component: RecipeListPageComponent
  },
  {
    path: 'parameter',
    component: ParameterDefinitionListPageComponent
  },
  {
    path: 'parameter/new',
    component: ParameterDefinitionCreatePageComponent
  },
  {
    path: 'parameter/:id',
    component: ParameterDefinitionDetailPageComponent
  },
  {
    path: 'parameter/:id/edit',
    component: ParameterDefinitionEditPageComponent
  },
  {
    path: 'new',
    component: RecipeCreatePageComponent
  },
  {
    path: 'new-derived',
    component: RecipeDerivedCreatePageComponent
  },
  {
    path: 'rules',
    component: DependencyRuleListPageComponent
  },
  {
    path: 'rules/new',
    component: DependencyRuleCreatePageComponent
  },
  {
    path: 'rules/:id',
    component: DependencyRuleDetailPageComponent
  },
  {
    path: 'rules/:id/edit',
    component: DependencyRuleEditPageComponent
  },
  {
    path: ':id/edit',
    component: RecipeEditPageComponent
  },
  {
    path: ':id',
    component: RecipeDetailPageComponent
  }
];