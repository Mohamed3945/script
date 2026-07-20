import { Component, HostListener } from '@angular/core';
import { TopNavbarComponent } from '../top-navbar/top-navbar.component';

@Component({
  selector: 'app-app-shell',
  standalone: true,
  imports: [TopNavbarComponent],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss'
})
export class AppShellComponent {
  readonly minSidebarWidth = 240;
  readonly maxSidebarWidth = 520;
  readonly collapsedSidebarWidth = 78;

  sidebarWidth = 300;
  isSidebarOpen = true;
  isResizing = false;

  private resizeStartX = 0;
  private resizeStartWidth = 300;

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
    if (this.isSidebarOpen && this.sidebarWidth < this.minSidebarWidth) {
      this.sidebarWidth = this.minSidebarWidth;
    }
  }

  startResize(event: MouseEvent): void {
    if (!this.isSidebarOpen || event.button !== 0) return;

    event.preventDefault();

    this.isResizing = true;
    this.resizeStartX = event.clientX;
    this.resizeStartWidth = this.sidebarWidth;
  }

  @HostListener('window:mousemove', ['$event'])
  onWindowMouseMove(event: MouseEvent): void {
    if (!this.isResizing) return;

    const deltaX = event.clientX - this.resizeStartX;
    const nextWidth = this.resizeStartWidth + deltaX;
    this.sidebarWidth = Math.min(this.maxSidebarWidth, Math.max(this.minSidebarWidth, nextWidth));
  }

  @HostListener('window:mouseup')
  onWindowMouseUp(): void {
    this.isResizing = false;
  }

  ngOnDestroy(): void {
    this.isResizing = false;
  }
}