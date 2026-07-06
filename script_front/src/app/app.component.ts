import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AppShellComponent } from './shared/components/app-shell/app-shell.component';
import { HttpFeedbackModalComponent } from './shared/components/http-feedback-modal/http-feedback-modal.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AppShellComponent, HttpFeedbackModalComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {}