import { Routes } from '@angular/router';
import { DependencyRuleCreatePageComponent } from '../recipe-management/pages/dependency-rule-create-page/dependency-rule-create-page.component';
import { DependencyRuleDetailPageComponent } from '../recipe-management/pages/dependency-rule-detail-page/dependency-rule-detail-page.component';
import { DependencyRuleEditPageComponent } from '../recipe-management/pages/dependency-rule-edit-page/dependency-rule-edit-page.component';
import { DependencyRuleListPageComponent } from '../recipe-management/pages/dependency-rule-list-page/dependency-rule-list-page.component';

export const RULES_MANAGEMENT_ROUTES: Routes = [
  {
    path: '',
    component: DependencyRuleListPageComponent
  },
  {
    path: 'new',
    component: DependencyRuleCreatePageComponent
  },
  {
    path: ':id',
    component: DependencyRuleDetailPageComponent
  },
  {
    path: ':id/edit',
    component: DependencyRuleEditPageComponent
  }
];