import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ConfigurationDefinition } from '../../../../../core/models/configuration-definition.model';

@Component({
  selector: 'app-definition-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './definition-table.component.html',
  styleUrl: './definition-table.component.scss'
})
export class DefinitionTableComponent {
  @Input() definitions: ConfigurationDefinition[] = [];
  @Output() edit = new EventEmitter<ConfigurationDefinition>();
  @Output() delete = new EventEmitter<ConfigurationDefinition>();
}
