import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [MatButtonModule, MatCardModule, RouterLink],
  template: `
    <mat-card>
      <mat-card-content>
        <p class="py-4 text-lg">Page not found.</p>
      </mat-card-content>
      <mat-card-actions>
        <a matButton routerLink="/">Go to start page</a>
      </mat-card-actions>
    </mat-card>
  `,
})
export class NotFoundComponent {}
