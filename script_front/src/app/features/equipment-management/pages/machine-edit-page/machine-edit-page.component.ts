import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { Machine } from '../../../../core/models/machine.model';
import { MachineDetail } from '../../../../core/models/machine-detail.model';
import { MachineApiService } from '../../../../core/services/machine-api.service';
import { MachineFormComponent } from '../../components/machines/machine-form/machine-form.component';

@Component({
  selector: 'app-machine-edit-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, MachineFormComponent],
  templateUrl: './machine-edit-page.component.html',
  styleUrl: './machine-edit-page.component.scss'
})
export class MachineEditPageComponent implements OnInit {
  machine$ = new BehaviorSubject<Machine | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private machineApiService: MachineApiService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.machineApiService.getMachine(id).subscribe({
      next: (machine: MachineDetail) => {
        this.machine$.next({
          id: machine.id,
          code: machine.code,
          name: machine.name,
          platformType: machine.platformType
        });
      },
      error: (error) => console.error('Failed to load machine', error)
    });
  }

  onSubmit(machine: Machine): void {
    const current = this.machine$.value;
    if (!current?.id) return;

    this.machineApiService.updateMachine(current.id, machine).subscribe({
      next: () => this.router.navigate(['/machines', current.id]),
      error: (error) => console.error('Failed to update machine', error)
    });
  }
}
