import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { AsyncPipe, NgIf } from '@angular/common';
import { ChamberCapability } from '../../../../core/models/chamber-capability.model';
import { ChamberCapabilityApiService } from '../../../../core/services/chamber-capability-api.service';
import { CapabilityTableComponent } from '../../components/capabilities/capability-table/capability-table.component';

@Component({
  selector: 'app-capability-list-page',
  standalone: true,
  imports: [AsyncPipe, NgIf, CapabilityTableComponent],
  templateUrl: './capability-list-page.component.html',
  styleUrl: './capability-list-page.component.scss'
})
export class CapabilityListPageComponent implements OnInit {
  capabilities$ = new BehaviorSubject<ChamberCapability[]>([]);

  constructor(
    private router: Router,
    private chamberCapabilityApiService: ChamberCapabilityApiService
  ) {}

  ngOnInit(): void {
    this.loadCapabilities();
  }

  loadCapabilities(): void {
    this.chamberCapabilityApiService.getCapabilities().subscribe({
      next: (capabilities) => this.capabilities$.next(capabilities),
      error: (error) => console.error('Failed to load capabilities', error)
    });
  }

  createCapability(): void {
    this.router.navigate(['/chamber-capabilities/new']);
  }

  editCapability(capability: ChamberCapability): void {
    if (!capability.id) return;
    this.router.navigate(['/chamber-capabilities', capability.id, 'edit']);
  }

  deleteCapability(capability: ChamberCapability): void {
    if (!capability.id) return;
    const confirmed = window.confirm(`Delete capability "${capability.code}"?`);
    if (!confirmed) return;

    this.chamberCapabilityApiService.deleteCapability(capability.id).subscribe({
      next: () => this.loadCapabilities(),
      error: (error) => console.error('Failed to delete capability', error)
    });
  }
}
