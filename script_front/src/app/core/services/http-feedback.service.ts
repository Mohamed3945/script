import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { HttpFeedbackMessage } from '../models/http-feedback-message.model';

@Injectable({ providedIn: 'root' })
/**
 * HttpFeedbackService coordinates UI logic for this feature.
 */
export class HttpFeedbackService {
  private readonly queue: HttpFeedbackMessage[] = [];
  private readonly currentSubject = new BehaviorSubject<HttpFeedbackMessage | null>(null);

  readonly current$ = this.currentSubject.asObservable();

  /**
   * Handles the push workflow.
   */
  push(message: HttpFeedbackMessage): void {
    if (this.currentSubject.value === null) {
      this.currentSubject.next(message);
      return;
    }

    this.queue.push(message);
  }

  /**
   * Handles the closeCurrent workflow.
   */
  closeCurrent(): void {
    const next = this.queue.shift() ?? null;
    this.currentSubject.next(next);
  }
}
