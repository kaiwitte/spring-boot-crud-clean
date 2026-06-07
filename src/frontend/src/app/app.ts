import { Component, inject, signal } from '@angular/core';
import { RoomService, type RoomResponse } from '../src/generated';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly roomService = inject(RoomService);

  protected readonly isSubmitting = signal(false);
  protected readonly status = signal('Ready');
  protected readonly createdRoom = signal<RoomResponse | null>(null);

  //UNREVIEWED_AI_CODE
  protected createRoom(): void {
    this.isSubmitting.set(true);
    this.status.set('Creating room...');
    this.createdRoom.set(null);

    this.roomService
      .newRoom({ name: `Room ${new Date().toISOString()}` })
      .subscribe({
        next: (room) => {
          this.createdRoom.set(room);
          this.status.set(`Created room ${room.id}`);
          this.isSubmitting.set(false);
        },
        error: () => {
          this.status.set('Request failed');
          this.isSubmitting.set(false);
        },
      });
  }
}
