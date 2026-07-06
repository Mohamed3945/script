import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ChamberDetail } from '../../../../core/models/chamber-detail.model';
import { ChamberCapability } from '../../../../core/models/chamber-capability.model';
import { ChamberConfiguration } from '../../../../core/models/chamber-configuration.model';
import { ChamberApiService } from '../../../../core/services/chamber-api.service';
import { ChamberCapabilityApiService } from '../../../../core/services/chamber-capability-api.service';
import { ChamberConfigurationApiService } from '../../../../core/services/chamber-configuration-api.service';
import { ChamberSummaryCardComponent } from '../../components/chambers/chamber-summary-card/chamber-summary-card.component';
import { ChamberCapabilityTableComponent } from '../../components/chambers/chamber-capability-table/chamber-capability-table.component';
import { ChamberConfigurationTableComponent } from '../../components/chambers/chamber-configuration-table/chamber-configuration-table.component';
import { ChamberConfigurationFormComponent } from '../../components/chambers/chamber-configuration-form/chamber-configuration-form.component';

@Component({
  selector: 'app-chamber-detail-page',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    AsyncPipe,
    ChamberSummaryCardComponent,
    ChamberCapabilityTableComponent,
    ChamberConfigurationTableComponent,
    ChamberConfigurationFormComponent
  ],
  templateUrl: './chamber-detail-page.component.html',
  styleUrl: './chamber-detail-page.component.scss'
})
export class ChamberDetailPageComponent implements OnInit {
  chamber$ = new BehaviorSubject<ChamberDetail | null>(null);
  allCapabilities$ = new BehaviorSubject<ChamberCapability[]>([]);
  showCapabilityModal = false;
  showConfigurationForm = false;
  editingConfiguration: ChamberConfiguration | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private chamberApiService: ChamberApiService,
    private chamberCapabilityApiService: ChamberCapabilityApiService,
    private chamberConfigurationApiService: ChamberConfigurationApiService
  ) {}

  ngOnInit(): void {
    this.loadChamber();
    this.loadAllCapabilities();
  }

  loadChamber(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.chamberApiService.getChamber(id).subscribe({
      next: (chamber) => this.chamber$.next(chamber),
      error: (error) => console.error('Failed to load chamber detail', error)
    });
  }

  loadAllCapabilities(): void {
    this.chamberCapabilityApiService.getCapabilities().subscribe({
      next: (capabilities) => this.allCapabilities$.next(capabilities),
      error: (error) => console.error('Failed to load capabilities', error)
    });
  }

  goToEdit(): void {
    const chamber = this.chamber$.value;
    if (!chamber) return;
    this.router.navigate(['/chambers', chamber.id, 'edit']);
  }

  goBackToMachine(): void {
    const chamber = this.chamber$.value;
    if (!chamber) return;
    this.router.navigate(['/machines', chamber.machineId]);
  }

  assignCapability(capability: ChamberCapability): void {
    const chamber = this.chamber$.value;
    if (!chamber || !capability.id) return;

    this.chamberApiService.addCapabilityToChamber(chamber.id, capability.id).subscribe({
      next: () => {
        this.loadChamber();
        this.closeCapabilityModal();
      },
      error: (error) => console.error('Failed to assign capability', error)
    });
  }

  removeCapability(capability: ChamberCapability): void {
    const chamber = this.chamber$.value;
    if (!chamber || !capability.id) return;

    this.chamberApiService.removeCapabilityFromChamber(chamber.id, capability.id).subscribe({
      next: () => this.loadChamber(),
      error: (error) => console.error('Failed to remove capability', error)
    });
  }

  openCapabilityModal(): void {
    this.showCapabilityModal = true;
  }

  closeCapabilityModal(): void {
    this.showCapabilityModal = false;
  }

  openCreateConfiguration(): void {
    this.editingConfiguration = null;
    this.showConfigurationForm = true;
  }

  openEditConfiguration(configuration: ChamberConfiguration): void {
    this.editingConfiguration = configuration;
    this.showConfigurationForm = true;
  }

  closeConfigurationForm(): void {
    this.showConfigurationForm = false;
    this.editingConfiguration = null;
  }

  saveConfiguration(configuration: ChamberConfiguration): void {
    const chamber = this.chamber$.value;
    if (!chamber) return;

    const request$ = configuration.id
      ? this.chamberConfigurationApiService.updateChamberConfiguration(configuration.id, configuration)
      : this.chamberConfigurationApiService.createChamberConfiguration(chamber.id, configuration);

    request$.subscribe({
      next: () => {
        this.closeConfigurationForm();
        this.loadChamber();
      },
      error: (error) => console.error('Failed to save chamber configuration', error)
    });
  }

  deleteConfiguration(configuration: ChamberConfiguration): void {
    if (!configuration.id) return;

    const confirmed = window.confirm(
      `Delete chamber configuration "${configuration.chamberConfigurationCode}"?`
    );
    if (!confirmed) return;

    this.chamberConfigurationApiService.deleteChamberConfiguration(configuration.id).subscribe({
      next: () => this.loadChamber(),
      error: (error) => console.error('Failed to delete chamber configuration', error)
    });
  }

  get assignableCapabilities(): ChamberCapability[] {
    const chamber = this.chamber$.value;
    const all = this.allCapabilities$.value;
    if (!chamber) return [];

    const assignedIds = new Set(chamber.capabilities.map(c => c.id));
    return all.filter(capability => capability.id && !assignedIds.has(capability.id));
  }
}
