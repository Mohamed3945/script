import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RecipeRequirements } from '../../../../../core/models/recipe-requirements.model';
import { ChamberCapability } from '../../../../../core/models/chamber-capability.model';
import { ConfigurationDefinition } from '../../../../../core/models/configuration-definition.model';

@Component({
  selector: 'app-recipe-requirements-panel',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './recipe-requirements-panel.component.html',
  styleUrl: './recipe-requirements-panel.component.scss'
})
export class RecipeRequirementsPanelComponent {
  @Input() requirements: RecipeRequirements | null = null;
  @Input() availableCapabilities: ChamberCapability[] = [];
  @Input() availableConfigurationDefinitions: ConfigurationDefinition[] = [];
  @Input() readOnly = false;

  @Output() addCapability = new EventEmitter<number>();
  @Output() removeCapability = new EventEmitter<number>();
  @Output() addConfiguration = new EventEmitter<number>();
  @Output() removeConfiguration = new EventEmitter<number>();

  showCapabilityModal = false;
  showConfigurationModal = false;

  openCapabilityModal(): void {
    this.showCapabilityModal = true;
  }

  closeCapabilityModal(): void {
    this.showCapabilityModal = false;
  }

  openConfigurationModal(): void {
    this.showConfigurationModal = true;
  }

  closeConfigurationModal(): void {
    this.showConfigurationModal = false;
  }

  get assignableCapabilities(): ChamberCapability[] {
    if (!this.requirements) return this.availableCapabilities;
    const assignedIds = new Set(this.requirements.requiredCapabilities.map(c => c.capabilityId));
    return this.availableCapabilities.filter(c => c.id && !assignedIds.has(c.id));
  }

  get assignableConfigurations(): ConfigurationDefinition[] {
    if (!this.requirements) return this.availableConfigurationDefinitions;
    const assignedIds = new Set(this.requirements.requiredConfigurations.map(c => c.configurationDefinitionId));
    return this.availableConfigurationDefinitions.filter(c => c.id && !assignedIds.has(c.id));
  }

  onAddCapability(capabilityId?: number): void {
    if (this.readOnly) return;
    if (!capabilityId) return;
    this.addCapability.emit(capabilityId);
    this.closeCapabilityModal();
  }

  onAddConfiguration(configurationDefinitionId?: number): void {
    if (this.readOnly) return;
    if (!configurationDefinitionId) return;
    this.addConfiguration.emit(configurationDefinitionId);
    this.closeConfigurationModal();
  }
}
