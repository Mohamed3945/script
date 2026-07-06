import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ChamberCapability } from '../../../../core/models/chamber-capability.model';
import { ChamberCapabilityApiService } from '../../../../core/services/chamber-capability-api.service';
import { CapabilityFormComponent } from '../../components/capabilities/capability-form/capability-form.component';

@Component({
  selector: 'app-capability-create-page',
  standalone: true,
  imports: [CapabilityFormComponent],
  templateUrl: './capability-create-page.component.html',
  styleUrl: './capability-create-page.component.scss'
})
export class CapabilityCreatePageComponent {
  constructor(
    private router: Router,
    private chamberCapabilityApiService: ChamberCapabilityApiService
  ) {}

  onSubmit(capability: ChamberCapability): void {
    this.chamberCapabilityApiService.createCapability(capability).subscribe({
      next: (created) => {
        if (created.id) {
          this.router.navigate(['/chamber-capabilities', created.id]);
        } else {
          this.router.navigate(['/chamber-capabilities']);
        }
      },
      error: (error) => console.error('Failed to create capability', error)
    });
  }
}
