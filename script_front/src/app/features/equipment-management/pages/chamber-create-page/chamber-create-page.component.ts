import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Chamber } from '../../../../core/models/chamber.model';
import { ChamberApiService } from '../../../../core/services/chamber-api.service';
import { ChamberFormComponent } from '../../components/chambers/chamber-form/chamber-form.component';

@Component({
  selector: 'app-chamber-create-page',
  standalone: true,
  imports: [ChamberFormComponent],
  templateUrl: './chamber-create-page.component.html',
  styleUrl: './chamber-create-page.component.scss'
})
export class ChamberCreatePageComponent implements OnInit {
  machineId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private chamberApiService: ChamberApiService
  ) {}

  ngOnInit(): void {
    this.machineId = Number(this.route.snapshot.paramMap.get('machineId'));
  }

  onSubmit(chamber: Chamber): void {
    this.chamberApiService.createChamber(this.machineId, chamber).subscribe({
      next: (created) => {
        if (created.id) {
          this.router.navigate(['/chambers', created.id]);
        }
      },
      error: (error) => console.error('Failed to create chamber', error)
    });
  }
}
