import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';

import {
  AbstractDeleteDialogComponent,
  DeleteDialogData,
} from './abstract-delete-dialog.component';

/** Opens the shared delete confirmation dialog; emits whether it deleted. */
@Injectable({ providedIn: 'root' })
export class DeleteFlowService {
  private readonly dialog = inject(MatDialog);

  confirm(data: DeleteDialogData): Observable<boolean> {
    return this.dialog
      .open<AbstractDeleteDialogComponent, DeleteDialogData, boolean>(
        AbstractDeleteDialogComponent,
        { data, disableClose: true, width: '420px' },
      )
      .afterClosed()
      .pipe(map((result) => result === true));
  }
}
