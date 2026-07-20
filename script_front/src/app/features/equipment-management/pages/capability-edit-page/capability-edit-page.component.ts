import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ChamberCapability } from '../../../../core/models/chamber-capability.model';
import { ChamberCapabilityApiService } from '../../../../core/services/chamber-capability-api.service';
import { CapabilityFormComponent } from '../../components/capabilities/capability-form/capability-form.component';

@Component({
  selector: 'app-capability-edit-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, CapabilityFormComponent],
  templateUrl: './capability-edit-page.component.html',
  styleUrl: './capability-edit-page.component.scss'
})
export class CapabilityEditPageComponent implements OnInit {
  capability$ = new BehaviorSubject<ChamberCapability | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private chamberCapabilityApiService: ChamberCapabilityApiService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.chamberCapabilityApiService.getCapability(id).subscribe({
      next: (capability) => this.capability$.next(capability),
      error: (error) => console.error('Failed to load capability', error)
    });
  }

  onSubmit(capability: ChamberCapability): void {
    const current = this.capability$.value;
    if (!current?.id) return;

    this.chamberCapabilityApiService.updateCapability(current.id, capability).subscribe({
      next: () => this.router.navigate(['/reference-data/capabilities']),
      error: (error) => console.error('Failed to update capability', error)
    });
  }
}
