import { Routes } from '@angular/router';
import { CapabilityCreatePageComponent } from '../equipment-management/pages/capability-create-page/capability-create-page.component';
import { CapabilityEditPageComponent } from '../equipment-management/pages/capability-edit-page/capability-edit-page.component';
import { CapabilityListPageComponent } from '../equipment-management/pages/capability-list-page/capability-list-page.component';
import { ConfigurationDefinitionCreatePageComponent } from '../equipment-management/pages/configuration-definition-create-page/configuration-definition-create-page.component';
import { ConfigurationDefinitionDetailPageComponent } from '../equipment-management/pages/configuration-definition-detail-page/configuration-definition-detail-page.component';
import { ConfigurationDefinitionEditPageComponent } from '../equipment-management/pages/configuration-definition-edit-page/configuration-definition-edit-page.component';
import { ConfigurationDefinitionListPageComponent } from '../equipment-management/pages/configuration-definition-list-page/configuration-definition-list-page.component';

export const REFERENCE_DATA_MANAGEMENT_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'capabilities',
    pathMatch: 'full'
  },
  {
    path: 'capabilities',
    component: CapabilityListPageComponent
  },
  {
    path: 'capabilities/new',
    component: CapabilityCreatePageComponent
  },
  {
    path: 'capabilities/:id/edit',
    component: CapabilityEditPageComponent
  },
  {
    path: 'configuration-definitions',
    component: ConfigurationDefinitionListPageComponent
  },
  {
    path: 'configuration-definitions/new',
    component: ConfigurationDefinitionCreatePageComponent
  },
  {
    path: 'configuration-definitions/:id',
    component: ConfigurationDefinitionDetailPageComponent
  },
  {
    path: 'configuration-definitions/:id/edit',
    component: ConfigurationDefinitionEditPageComponent
  }
];