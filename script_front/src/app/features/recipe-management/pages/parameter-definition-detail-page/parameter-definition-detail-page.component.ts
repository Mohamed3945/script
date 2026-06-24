import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ParameterDefinitionDetail } from '../../../../core/models/parameter-definition-detail.model';
import { ParameterOption } from '../../../../core/models/parameter-option.model';
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
    AsyncPipe,
    ParameterDefinitionSummaryComponent,
    ParameterOptionTableComponent,
    ParameterOptionFormComponent
  ],
  templateUrl: './parameter-definition-detail-page.component.html',
  styleUrl: './parameter-definition-detail-page.component.scss'
})
export class ParameterDefinitionDetailPageComponent implements OnInit {
  detail$ = new BehaviorSubject<ParameterDefinitionDetail | null>(null);
  loading$ = new BehaviorSubject<boolean>(false);
  editingOption: ParameterOption | null = null;
  showOptionForm = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private parameterDefinitionApiService: ParameterDefinitionApiService,
    private parameterOptionApiService: ParameterOptionApiService
  ) {}

  ngOnInit(): void {
    this.loadDetail();
  }

  loadDetail(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.loading$.next(true);
    this.parameterDefinitionApiService.getDefinitionDetail(id).subscribe({
      next: (detail) => {
        this.detail$.next(detail);
        this.loading$.next(false);
      },
      error: (error) => {
        console.error('Failed to load parameter definition detail', error);
        this.loading$.next(false);
      }
    });
  }

  onEditDefinition(): void {
    const detail = this.detail$.value;
    if (!detail?.id) return;
    this.router.navigate(['/recipes/parameter-definitions', detail.id, 'edit']);
  }

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

  onAddOption(): void {
    this.editingOption = null;
    this.showOptionForm = true;
  }

  onEditOption(option: ParameterOption): void {
    this.editingOption = option;
    this.showOptionForm = true;
  }

  onDeleteOption(option: ParameterOption): void {
    if (!option.id) return;

    const confirmed = window.confirm(`Delete option "${option.label}"?`);
    if (!confirmed) return;

    this.parameterOptionApiService.deleteOption(option.id).subscribe({
      next: () => this.loadDetail(),
      error: (error) => console.error('Failed to delete parameter option', error)
    });
  }

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

  onOptionCancel(): void {
    this.showOptionForm = false;
    this.editingOption = null;
  }
}

