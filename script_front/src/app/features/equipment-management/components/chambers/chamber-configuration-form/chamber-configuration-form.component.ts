import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { NgFor } from '@angular/common';
import { ChamberConfiguration } from '../../../../../core/models/chamber-configuration.model';
import { ConfigurationDefinition } from '../../../../../core/models/configuration-definition.model';
import { ConfigurationDefinitionApiService } from '../../../../../core/services/configuration-definition-api.service';

@Component({
  selector: 'app-chamber-configuration-form',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor],
  templateUrl: './chamber-configuration-form.component.html',
  styleUrl: './chamber-configuration-form.component.scss'
})
export class ChamberConfigurationFormComponent implements OnInit {
  @Input() initialValue: ChamberConfiguration | null = null;
  @Input() chamberId!: number;
  @Input() mode: 'create' | 'edit' = 'create';

  @Output() submitted = new EventEmitter<ChamberConfiguration>();

  definitions: ConfigurationDefinition[] = [];
  form: any;

  constructor(
    private fb: FormBuilder,
    private configurationDefinitionApiService: ConfigurationDefinitionApiService
  ) {
    this.form = this.fb.group({
      configurationDefinitionId: [null as number | null, Validators.required],
      chamberConfigurationCode: ['', Validators.required],
      chamberConfigurationName: ['', Validators.required],
      nominalValue: [null as number | null],
      minValue: [null as number | null],
      maxValue: [null as number | null]
    });
  }

  ngOnInit(): void {
    this.configurationDefinitionApiService.getDefinitions().subscribe({
      next: (definitions) => {
        this.definitions = definitions;
      },
      error: (error) => console.error('Failed to load configuration definitions', error)
    });

    if (this.initialValue) {
      this.form.patchValue({
        configurationDefinitionId: this.initialValue.configurationDefinitionId,
        chamberConfigurationCode: this.initialValue.chamberConfigurationCode,
        chamberConfigurationName: this.initialValue.chamberConfigurationName,
        nominalValue: this.initialValue.nominalValue ?? null,
        minValue: this.initialValue.minValue ?? null,
        maxValue: this.initialValue.maxValue ?? null
      });
    }
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
      chamberConfigurationCode: raw.chamberConfigurationCode || '',
      chamberConfigurationName: raw.chamberConfigurationName || '',
      nominalValue: raw.nominalValue,
      minValue: raw.minValue,
      maxValue: raw.maxValue
    });
  }
}
