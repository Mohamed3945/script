import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ParameterDefinition } from '../../../../core/models/parameter-definition.model';
import { ParameterDefinitionApiService } from '../../../../core/services/parameter-definition-api.service';
import { ParameterDefinitionFormComponent } from '../../components/parameter-definitions/parameter-definition-form/parameter-definition-form.component';

@Component({
  selector: 'app-parameter-definition-create-page',
  standalone: true,
  imports: [ParameterDefinitionFormComponent],
  templateUrl: './parameter-definition-create-page.component.html',
  styleUrl: './parameter-definition-create-page.component.scss'
})
/**
 * ParameterDefinitionCreatePageComponent coordinates UI logic for this feature.
 */
export class ParameterDefinitionCreatePageComponent {
  constructor(
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private router: Router
  ) {}

  /**
   * Handles the onSubmit workflow.
   */
  onSubmit(definition: ParameterDefinition): void {
    this.parameterDefinitionApiService.createDefinition(definition).subscribe({
      next: (created) => {
        if (created.id) {
          this.router.navigate(['/parameters', created.id]);
        } else {
          this.router.navigate(['/parameters']);
        }
      },
      error: (error) => {
        console.error('Failed to create parameter definition', error);
      }
    });
  }
}

