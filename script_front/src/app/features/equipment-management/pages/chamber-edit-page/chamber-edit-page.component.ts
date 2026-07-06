import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { Chamber } from '../../../../core/models/chamber.model';
import { ChamberDetail } from '../../../../core/models/chamber-detail.model';
import { ChamberApiService } from '../../../../core/services/chamber-api.service';
import { ChamberFormComponent } from '../../components/chambers/chamber-form/chamber-form.component';

@Component({
  selector: 'app-chamber-edit-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, ChamberFormComponent],
  templateUrl: './chamber-edit-page.component.html',
  styleUrl: './chamber-edit-page.component.scss'
})
export class ChamberEditPageComponent implements OnInit {
  chamber$ = new BehaviorSubject<Chamber | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private chamberApiService: ChamberApiService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.chamberApiService.getChamber(id).subscribe({
      next: (chamber: ChamberDetail) => {
        this.chamber$.next({
          id: chamber.id,
          machineId: chamber.machineId,
          machineCode: chamber.machineCode,
          machineName: chamber.machineName,
          code: chamber.code,
          name: chamber.name
        });
      },
      error: (error) => console.error('Failed to load chamber', error)
    });
  }

  onSubmit(chamber: Chamber): void {
    const current = this.chamber$.value;
    if (!current?.id) return;

    this.chamberApiService.updateChamber(current.id, chamber).subscribe({
      next: () => this.router.navigate(['/chambers', current.id]),
      error: (error) => console.error('Failed to update chamber', error)
    });
  }
}
