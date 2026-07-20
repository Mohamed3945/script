import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RecipeCompatibilityRefreshService {
  private readonly refreshSubject = new Subject<number>();

  readonly refresh$ = this.refreshSubject.asObservable();

  requestRefresh(recipeId: number): void {
    if (recipeId == null || Number.isNaN(recipeId)) {
      return;
    }
    this.refreshSubject.next(recipeId);
  }
}