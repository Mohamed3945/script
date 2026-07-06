import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { Machine } from '../../../../core/models/machine.model';
import { MachineApiService } from '../../../../core/services/machine-api.service';
import { MachineTableComponent } from '../../components/machines/machine-table/machine-table.component';

@Component({
  selector: 'app-machine-list-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, MachineTableComponent],
  templateUrl: './machine-list-page.component.html',
  styleUrl: './machine-list-page.component.scss'
})
export class MachineListPageComponent implements OnInit {
  machines$ = new BehaviorSubject<Machine[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);

  constructor(
    private machineApiService: MachineApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMachines();
  }

  loadMachines(): void {
    this.loading$.next(true);
    this.machineApiService.getMachines().subscribe({
      next: (machines) => {
        this.machines$.next(machines);
        this.loading$.next(false);
      },
      error: (error) => {
        console.error('Failed to load machines', error);
        this.loading$.next(false);
      }
    });
  }

  onCreate(): void {
    this.router.navigate(['/machines/new']);
  }

  onView(machine: Machine): void {
    if (!machine.id) return;
    this.router.navigate(['/machines', machine.id]);
  }

  onEdit(machine: Machine): void {
    if (!machine.id) return;
    this.router.navigate(['/machines', machine.id, 'edit']);
  }

  onDelete(machine: Machine): void {
    if (!machine.id) return;

    const confirmed = window.confirm(`Delete machine "${machine.code}"?`);
    if (!confirmed) return;

    this.machineApiService.deleteMachine(machine.id).subscribe({
      next: () => this.loadMachines(),
      error: (error) => console.error('Failed to delete machine', error)
    });
  }
}
