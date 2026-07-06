import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { MachineDetail } from '../../../../core/models/machine-detail.model';
import { MachineApiService } from '../../../../core/services/machine-api.service';
import { MachineSummaryCardComponent } from '../../components/machines/machine-summary-card/machine-summary-card.component';

@Component({
  selector: 'app-machine-detail-page',
  standalone: true,
  imports: [NgIf, NgFor, AsyncPipe, MachineSummaryCardComponent],
  templateUrl: './machine-detail-page.component.html',
  styleUrl: './machine-detail-page.component.scss'
})
export class MachineDetailPageComponent implements OnInit {
  machine$ = new BehaviorSubject<MachineDetail | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private machineApiService: MachineApiService
  ) {}

  ngOnInit(): void {
    this.loadMachine();
  }

  loadMachine(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.machineApiService.getMachine(id).subscribe({
      next: (machine) => this.machine$.next(machine),
      error: (error) => console.error('Failed to load machine detail', error)
    });
  }

  goToEdit(): void {
    const machine = this.machine$.value;
    if (!machine) return;
    this.router.navigate(['/machines', machine.id, 'edit']);
  }

  goToCreateChamber(): void {
    const machine = this.machine$.value;
    if (!machine) return;
    this.router.navigate(['/machines', machine.id, 'chambers', 'new']);
  }

  openChamber(chamberId?: number): void {
    if (!chamberId) return;
    this.router.navigate(['/chambers', chamberId]);
  }
}
