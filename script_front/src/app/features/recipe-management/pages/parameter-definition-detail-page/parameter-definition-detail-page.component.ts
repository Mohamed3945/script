import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ConfigurationDefinition } from '../../../../core/models/configuration-definition.model';
import { ParameterDefinition } from '../../../../core/models/parameter-definition.model';
import { ParameterDefinitionDetail } from '../../../../core/models/parameter-definition-detail.model';
import { ParameterOption } from '../../../../core/models/parameter-option.model';
import { ConfigurationDefinitionApiService } from '../../../../core/services/configuration-definition-api.service';
import { ParameterDefinitionApiService } from '../../../../core/services/parameter-definition-api.service';
import { ParameterOptionApiService } from '../../../../core/services/parameter-option-api.service';
import { ParameterDefinitionSummaryComponent } from '../../components/parameter-definitions/parameter-definition-summary/parameter-definition-summary.component';
import { ParameterOptionTableComponent } from '../../components/parameter-definitions/parameter-option-table/parameter-option-table.component';
import { ParameterOptionFormComponent } from '../../components/parameter-definitions/parameter-option-form/parameter-option-form.component';

@Component({
  selector: 'app-parameter-definition-detail-page',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    AsyncPipe,
    FormsModule,
    ParameterDefinitionSummaryComponent,
    ParameterOptionTableComponent,
    ParameterOptionFormComponent
  ],
  templateUrl: './parameter-definition-detail-page.component.html',
  styleUrl: './parameter-definition-detail-page.component.scss'
})
/**
 * ParameterDefinitionDetailPageComponent coordinates UI logic for this feature.
 */
export class ParameterDefinitionDetailPageComponent implements OnInit {
  detail$ = new BehaviorSubject<ParameterDefinitionDetail | null>(null);
  configurationDefinitions$ = new BehaviorSubject<ConfigurationDefinition[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  editingOption: ParameterOption | null = null;
  showOptionForm = false;
  showImpactForm = false;
  selectedConfigurationDefinitionId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private parameterOptionApiService: ParameterOptionApiService,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService
  ) {}

  /**
   * Handles the ngOnInit workflow.
   */
  ngOnInit(): void {
    this.loadConfigurationDefinitions();
    this.loadDetail();
  }

  /**
   * Handles the loadDetail workflow.
   */
  loadDetail(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.loading$.next(true);
    this.parameterDefinitionApiService.getDefinitionDetail(id).subscribe({
      next: (detail) => {
        this.detail$.next(detail);
        this.selectedConfigurationDefinitionId = detail.configurationDefinitionId ?? null;
        this.loading$.next(false);
      },
      error: (error) => {
        console.error('Failed to load parameter definition detail', error);
        this.loading$.next(false);
      }
    });
  }

  /**
   * Handles the onEditDefinition workflow.
   */
  onEditDefinition(): void {
    const detail = this.detail$.value;
    if (!detail?.id) return;
    this.router.navigate(['/recipes/parameter-definitions', detail.id, 'edit']);
  }

  /**
   * Handles the onDeleteDefinition workflow.
   */
  onDeleteDefinition(): void {
    const detail = this.detail$.value;
    if (!detail?.id) return;

    const confirmed = window.confirm(`Delete parameter definition "${detail.name}"?`);
    if (!confirmed) return;

    this.parameterDefinitionApiService.deleteDefinition(detail.id).subscribe({
      next: () => this.router.navigate(['/recipes/parameter-definitions']),
      error: (error) => console.error('Failed to delete parameter definition', error)
    });
  }

  /**
   * Handles the onAddOption workflow.
   */
  onAddOption(): void {
    this.editingOption = null;
    this.showOptionForm = true;
  }

  /**
   * Handles the onEditOption workflow.
   */
  onEditOption(option: ParameterOption): void {
    this.editingOption = option;
    this.showOptionForm = true;
  }

  /**
   * Handles the onDeleteOption workflow.
   */
  onDeleteOption(option: ParameterOption): void {
    if (!option.id) return;

    const confirmed = window.confirm(`Delete option "${option.label}"?`);
    if (!confirmed) return;

    this.parameterOptionApiService.deleteOption(option.id).subscribe({
      next: () => this.loadDetail(),
      error: (error) => console.error('Failed to delete parameter option', error)
    });
  }

  /**
   * Handles the onOptionSubmit workflow.
   */
  onOptionSubmit(option: ParameterOption): void {
    const request$ = option.id
      ? this.parameterOptionApiService.updateOption(option.id, option)
      : this.parameterOptionApiService.createOption(option);

    request$.subscribe({
      next: () => {
        this.showOptionForm = false;
        this.editingOption = null;
        this.loadDetail();
      },
      error: (error) => console.error('Failed to save parameter option', error)
    });
  }

  /**
   * Handles the onOptionCancel workflow.
   */
  onOptionCancel(): void {
    this.showOptionForm = false;
    this.editingOption = null;
  }

  onStartAddImpact(): void {
    this.showImpactForm = true;
    const detail = this.detail$.value;
    this.selectedConfigurationDefinitionId = detail?.configurationDefinitionId ?? null;
  }

  onCancelAddImpact(): void {
    this.showImpactForm = false;
    const detail = this.detail$.value;
    this.selectedConfigurationDefinitionId = detail?.configurationDefinitionId ?? null;
  }

  onSaveImpact(): void {
    const detail = this.detail$.value;
    if (!detail?.id) {
      return;
    }

    const payload = this.buildDefinitionPayload(detail, this.selectedConfigurationDefinitionId);
    this.parameterDefinitionApiService.updateDefinition(detail.id, payload).subscribe({
      next: () => {
        this.showImpactForm = false;
        this.loadDetail();
      },
      error: (error) => console.error('Failed to save impacted configuration', error)
    });
  }

  onRemoveImpact(): void {
    const detail = this.detail$.value;
    if (!detail?.id) {
      return;
    }

    const payload = this.buildDefinitionPayload(detail, null);
    this.parameterDefinitionApiService.updateDefinition(detail.id, payload).subscribe({
      next: () => {
        this.showImpactForm = false;
        this.selectedConfigurationDefinitionId = null;
        this.loadDetail();
      },
      error: (error) => console.error('Failed to remove impacted configuration', error)
    });
  }

  private loadConfigurationDefinitions(): void {
    this.configurationDefinitionApiService.getDefinitions().subscribe({
      next: (definitions) => this.configurationDefinitions$.next(definitions),
      error: (error) => {
        console.error('Failed to load configuration definitions', error);
        this.configurationDefinitions$.next([]);
      }
    });
  }

  private buildDefinitionPayload(
    detail: ParameterDefinitionDetail,
    configurationDefinitionId: number | null
  ): ParameterDefinition {
    return {
      id: detail.id,
      name: detail.name,
      alias: detail.alias,
      unit: detail.unit ?? null,
      description: detail.description ?? null,
      valueType: detail.valueType,
      requiredOnStep: detail.requiredOnStep,
      stepType: detail.stepType,
      defaultValueJson: detail.defaultValueJson ?? null,
      parameterGroup: detail.parameterGroup ?? null,
      parameterGroupOrder: detail.parameterGroupOrder ?? null,
      configurationDefinitionId
    };
  }
}

