import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Observable, finalize } from 'rxjs';

import { errorMessage } from '../api/api-error';

export interface DeleteDialogData {
  /** e.g. "Room" */
  readonly entityName: string;
  /** Human-readable label of the item about to be deleted. */
  readonly itemLabel: string;
  /** Deferred generated API delete call; subscribed on confirmation. */
  readonly performDelete: () => Observable<unknown>;
}

/**
 * Reusable confirmation dialog that performs the delete itself, showing
 * pending state and API errors inline. Closes with true on success.
 */
@Component({
  selector: 'app-abstract-delete-dialog',
  imports: [MatButtonModule, MatDialogModule, MatProgressSpinnerModule],
  templateUrl: './abstract-delete-dialog.component.html',
})
export class AbstractDeleteDialogComponent {
  protected readonly data = inject<DeleteDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<AbstractDeleteDialogComponent, boolean>);

  protected readonly pending = signal(false);
  protected readonly error = signal<string | null>(null);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    if (this.pending()) {
      return;
    }
    this.pending.set(true);
    this.error.set(null);
    this.data
      .performDelete()
      .pipe(finalize(() => this.pending.set(false)))
      .subscribe({
        next: () => this.dialogRef.close(true),
        error: (error: unknown) =>
          this.error.set(errorMessage(error, 'Deleting failed. Please try again.')),
      });
  }
}
