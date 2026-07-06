import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ConfigurationDefinition } from '../../../../core/models/configuration-definition.model';
import { ConfigurationDefinitionApiService } from '../../../../core/services/configuration-definition-api.service';
import { DefinitionFormComponent } from '../../components/definitions/definition-form/definition-form.component';

@Component({
  selector: 'app-definition-create-page',
  standalone: true,
  imports: [DefinitionFormComponent],
  templateUrl: './definition-create-page.component.html',
  styleUrl: './definition-create-page.component.scss'
})
export class DefinitionCreatePageComponent {
  constructor(
    private router: Router,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService
  ) {}

  onSubmit(definition: ConfigurationDefinition): void {
    this.configurationDefinitionApiService.createDefinition(definition).subscribe({
      next: (created) => {
        if (created.id) {
          this.router.navigate(['/configuration-definitions', created.id]);
        } else {
          this.router.navigate(['/configuration-definitions']);
        }
      },
      error: (error) => console.error('Failed to create definition', error)
    });
  }
}
