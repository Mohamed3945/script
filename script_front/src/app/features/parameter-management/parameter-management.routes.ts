import { Routes } from '@angular/router';
import { ParameterDefinitionCreatePageComponent } from '../recipe-management/pages/parameter-definition-create-page/parameter-definition-create-page.component';
import { ParameterDefinitionDetailPageComponent } from '../recipe-management/pages/parameter-definition-detail-page/parameter-definition-detail-page.component';
import { ParameterDefinitionEditPageComponent } from '../recipe-management/pages/parameter-definition-edit-page/parameter-definition-edit-page.component';
import { ParameterDefinitionListPageComponent } from '../recipe-management/pages/parameter-definition-list-page/parameter-definition-list-page.component';

export const PARAMETER_MANAGEMENT_ROUTES: Routes = [
  {
    path: '',
    component: ParameterDefinitionListPageComponent
  },
  {
    path: 'new',
    component: ParameterDefinitionCreatePageComponent
  },
  {
    path: ':id',
    component: ParameterDefinitionDetailPageComponent
  },
  {
    path: ':id/edit',
    component: ParameterDefinitionEditPageComponent
  }
];