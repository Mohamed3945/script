import { Routes } from '@angular/router';

import { UserManagementPageComponent } from './pages/user-management-page/user-management-page.component';

export const USER_MANAGEMENT_ROUTES: Routes = [
  {
    path: '',
    component: UserManagementPageComponent
  }
];