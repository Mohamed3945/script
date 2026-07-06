import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Machine } from '../../../../core/models/machine.model';
import { MachineApiService } from '../../../../core/services/machine-api.service';
import { MachineFormComponent } from '../../components/machines/machine-form/machine-form.component';

@Component({
  selector: 'app-machine-create-page',
  standalone: true,
  imports: [MachineFormComponent],
  templateUrl: './machine-create-page.component.html',
  styleUrl: './machine-create-page.component.scss'
})
export class MachineCreatePageComponent {
  constructor(
    private machineApiService: MachineApiService,
    private router: Router
  ) {}

  onSubmit(machine: Machine): void {
    this.machineApiService.createMachine(machine).subscribe({
      next: (created) => {
        if (created.id) {
          this.router.navigate(['/machines', created.id]);
        } else {
          this.router.navigate(['/machines']);
        }
      },
      error: (error) => console.error('Failed to create machine', error)
    });
  }
}
