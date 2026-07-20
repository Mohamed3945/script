import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { ChamberConfiguration } from '../../../../../core/models/chamber-configuration.model';
import { ConfigurationDefinition } from '../../../../../core/models/configuration-definition.model';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';
import { ConfigurationDefinitionApiService } from '../../../../../core/services/configuration-definition-api.service';
import { ParameterDefinitionApiService } from '../../../../../core/services/parameter-definition-api.service';

@Component({
  selector: 'app-chamber-configuration-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor, NgIf],
  templateUrl: './chamber-configuration-form.component.html',
  styleUrl: './chamber-configuration-form.component.scss'
})
export class ChamberConfigurationFormComponent implements OnInit {
  @Input() initialValue: ChamberConfiguration | null = null;
  @Input() chamberId!: number;
  @Input() mode: 'create' | 'edit' = 'create';

  @Output() submitted = new EventEmitter<ChamberConfiguration>();

  definitions: ConfigurationDefinition[] = [];
  availableControllerCodes: { value: string; label: string }[] = [];
  form: any;

  constructor(
    private fb: FormBuilder,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService,
    private parameterDefinitionApiService: ParameterDefinitionApiService
  ) {
    this.form = this.fb.group({
      configurationDefinitionId: [null as number | null, Validators.required],
      chamberConfigurationCode: ['', Validators.required],
      chamberConfigurationName: ['', Validators.required],
      nominalValue: [null as number | null],
      minValue:     [null as number | null],
      maxValue:     [null as number | null]
    });
  }

  ngOnInit(): void {
    this.configurationDefinitionApiService.getDefinitions().subscribe({
      next: (definitions) => {
        this.definitions = definitions;

        // En mode edit, charger les controllers de la configDef initiale
        if (this.initialValue?.configurationDefinitionId) {
          this.loadControllerCodes(
            this.initialValue.configurationDefinitionId,
            this.initialValue.chamberConfigurationCode
          );
        }
      },
      error: (error) => console.error('Failed to load configuration definitions', error)
    });

    // Réagir au changement de configurationDefinitionId
    this.form.get('configurationDefinitionId')
      ?.valueChanges
      .subscribe((configDefId: number | null) => {
        this.availableControllerCodes = [];
        this.form.get('chamberConfigurationCode')?.reset('');

        if (configDefId) {
          this.loadControllerCodes(configDefId, null);
        }
      });

    if (this.initialValue) {
      this.form.patchValue({
        configurationDefinitionId: this.initialValue.configurationDefinitionId,
        chamberConfigurationCode:  this.initialValue.chamberConfigurationCode,
        chamberConfigurationName:  this.initialValue.chamberConfigurationName,
        nominalValue: this.initialValue.nominalValue ?? null,
        minValue:     this.initialValue.minValue     ?? null,
        maxValue:     this.initialValue.maxValue     ?? null
      });
    }
  }

  private loadControllerCodes(
    configDefId: number,
    preselectedCode: string | null
  ): void {
    this.parameterDefinitionApiService
      .getByConfigurationDefinition(configDefId)
      .subscribe({
        next: (paramDefs: ParameterDefinition[]) => {
          this.availableControllerCodes = paramDefs
            .filter(pd => pd.code != null)
            .map(pd => ({
              value: pd.code as string,
              label: pd.alias ?? pd.code as string
            }));

          // En mode edit : rétablir la sélection après rechargement
          if (preselectedCode) {
            this.form.get('chamberConfigurationCode')
              ?.setValue(preselectedCode, { emitEvent: false });
          }
        },
        error: (error) =>
          console.error('Failed to load parameter definitions for config def', error)
      });
  }

  onSubmit(): void {
    if (this.form.invalid || !this.chamberId) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    this.submitted.emit({
      id: this.initialValue?.id,
      chamberId: this.chamberId,
      configurationDefinitionId: raw.configurationDefinitionId!,
      chamberConfigurationCode:  raw.chamberConfigurationCode || '',
      chamberConfigurationName:  raw.chamberConfigurationName || '',
      nominalValue: raw.nominalValue,
      minValue:     raw.minValue,
      maxValue:     raw.maxValue
    });
  }
}