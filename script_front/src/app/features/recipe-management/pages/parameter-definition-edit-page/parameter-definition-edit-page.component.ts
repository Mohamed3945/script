import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ParameterDefinition } from '../../../../core/models/parameter-definition.model';
import { ParameterDefinitionApiService } from '../../../../core/services/parameter-definition-api.service';
import { ParameterDefinitionFormComponent } from '../../components/parameter-definitions/parameter-definition-form/parameter-definition-form.component';

@Component({
  selector: 'app-parameter-definition-edit-page',
  standalone: true,
  imports: [NgIf, AsyncPipe, ParameterDefinitionFormComponent],
  templateUrl: './parameter-definition-edit-page.component.html',
  styleUrl: './parameter-definition-edit-page.component.scss'
})
/**
 * ParameterDefinitionEditPageComponent coordinates UI logic for this feature.
 */
export class ParameterDefinitionEditPageComponent implements OnInit {
  definition$ = new BehaviorSubject<ParameterDefinition | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private parameterDefinitionApiService: ParameterDefinitionApiService
  ) {}

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.parameterDefinitionApiService.getDefinition(id).subscribe({
      next: (definition) => this.definition$.next(definition),
      error: (error) => console.error('Failed to load parameter definition', error)
    });
  }

  /**
   * Handles the onSubmit workflow.
   */
  onSubmit(definition: ParameterDefinition): void {
    const current = this.definition$.value;
    if (!current?.id) return;

    this.parameterDefinitionApiService.updateDefinition(current.id, definition).subscribe({
      next: () => this.router.navigate(['/recipes/parameter-definitions', current.id]),
      error: (error) => console.error('Failed to update parameter definition', error)
    });
  }
}

