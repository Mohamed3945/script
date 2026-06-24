import { NgFor, TitleCasePipe } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

export type RecipeWorkspaceTab = 'summary' | 'prestep' | 'steps';

@Component({
  selector: 'app-recipe-tab-nav',
  standalone: true,
  imports: [NgFor, TitleCasePipe],
  templateUrl: './recipe-tab-nav.component.html',
  styleUrl: './recipe-tab-nav.component.scss'
})
export class RecipeTabNavComponent {
  @Input() activeTab: RecipeWorkspaceTab = 'steps';
  @Output() tabChanged = new EventEmitter<RecipeWorkspaceTab>();

  tabs: RecipeWorkspaceTab[] = ['summary', 'prestep', 'steps'];
}
