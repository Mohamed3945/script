import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ConfigurationDefinitionDetail } from '../../../../core/models/configuration-definition-detail.model';
import { ConfigurationDefinitionApiService } from '../../../../core/services/configuration-definition-api.service';

@Component({
  selector: 'app-configuration-definition-detail-page',
  standalone: true,
  imports: [NgIf, AsyncPipe],
  templateUrl: './configuration-definition-detail-page.component.html',
  styleUrl: './configuration-definition-detail-page.component.scss'
})
export class ConfigurationDefinitionDetailPageComponent implements OnInit {
  definition$ = new BehaviorSubject<ConfigurationDefinitionDetail | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.configurationDefinitionApiService.getDefinition(id).subscribe({
      next: (definition) => this.definition$.next(definition),
      error: (error) => console.error('Failed to load definition', error)
    });
  }

  goBack(): void {
    this.router.navigate(['/reference-data/configuration-definitions']);
  }

  editDefinition(): void {
    const definition = this.definition$.value;
    if (!definition) return;
    this.router.navigate(['/reference-data/configuration-definitions', definition.id, 'edit']);
  }
}
