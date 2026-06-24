import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ParameterDefinition } from '../../../../../core/models/parameter-definition.model';

@Component({
  selector: 'app-parameter-definition-table',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './parameter-definition-table.component.html',
  styleUrl: './parameter-definition-table.component.scss'
})
export class ParameterDefinitionTableComponent {
  @Input() definitions: ParameterDefinition[] = [];

  @Output() viewDefinition = new EventEmitter<ParameterDefinition>();
  @Output() editDefinition = new EventEmitter<ParameterDefinition>();
  @Output() deleteDefinition = new EventEmitter<ParameterDefinition>();
}

