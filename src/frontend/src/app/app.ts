import { Component, OnInit, inject, signal } from '@angular/core';
import { RoomService, type RoomResponse } from '../src/generated';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private readonly roomService = inject(RoomService);

  protected readonly isSubmitting = signal(false);
  protected readonly status = signal('Ready');
  protected readonly createdRoom = signal<RoomResponse | null>(null);
  protected readonly rooms = signal<RoomResponse[]>([]);

  ngOnInit(): void {
    this.loadRooms();
  }

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
          this.loadRooms();
        },
        error: () => {
          this.status.set('Request failed');
          this.isSubmitting.set(false);
        },
      });
  }

  private loadRooms(): void {
    this.roomService.listRoom().subscribe({
      next: (response) => this.rooms.set(response.results ?? []),
      error: () => this.status.set('Could not load rooms'),
    });
  }
}
