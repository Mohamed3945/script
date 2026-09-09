import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ConfigurationDefinition } from '../../../../core/models/configuration-definition.model';
import { ConfigurationDefinitionApiService } from '../../../../core/services/configuration-definition-api.service';
import { DefinitionFormComponent } from '../../components/definitions/definition-form/definition-form.component';

@Component({
  selector: 'app-definition-edit-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, DefinitionFormComponent],
  templateUrl: './definition-edit-page.component.html',
  styleUrl: './definition-edit-page.component.scss'
})
export class DefinitionEditPageComponent implements OnInit {
  definition$ = new BehaviorSubject<ConfigurationDefinition | null>(null);

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

  onSubmit(definition: ConfigurationDefinition): void {
    const current = this.definition$.value;
    if (!current?.id) return;

    this.configurationDefinitionApiService.updateDefinition(current.id, definition).subscribe({
      next: () => this.router.navigate(['/reference-data/configuration-definitions', current.id]),
      error: (error) => console.error('Failed to update definition', error)
    });
  }
}
