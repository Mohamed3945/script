import { AsyncPipe, NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { HttpFeedbackService } from '../../../core/services/http-feedback.service';

@Component({
  selector: 'app-http-feedback-modal',
  standalone: true,
  imports: [NgIf, AsyncPipe],
  templateUrl: './http-feedback-modal.component.html',
  styleUrl: './http-feedback-modal.component.scss'
})
export class HttpFeedbackModalComponent {
  constructor(public feedbackService: HttpFeedbackService) {}

  close(): void {
    this.feedbackService.closeCurrent();
  }
}
