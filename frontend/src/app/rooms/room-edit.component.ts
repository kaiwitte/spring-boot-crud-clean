import { Component, viewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';

import { RoomRequest, RoomResponse } from '@generated';

import { AbstractEditorComponent } from '../shared/abstract-editor/abstract-editor.component';
import { AbstractItemShellComponent } from '../shared/abstract-item/abstract-item-shell.component';
import { RoomFormComponent } from './room-form.component';

@Component({
  selector: 'app-room-edit',
  imports: [AbstractItemShellComponent, MatCardModule, RoomFormComponent],
  template: `
    <app-abstract-item-shell
      [state]="state()"
      [entityName]="config.names.singular"
      [backLink]="listLink"
    >
      <mat-card>
        <mat-card-header>
          <mat-card-title>Edit {{ config.names.singular }}</mat-card-title>
        </mat-card-header>
        <mat-card-content class="pt-4">
          <app-room-form
            [initial]="item()"
            [pending]="pending()"
            [generalErrors]="generalErrors()"
            [cancelLink]="cancelLink()"
            submitLabel="Save"
            (save)="submit($event)"
          />
        </mat-card-content>
      </mat-card>
    </app-abstract-item-shell>
  `,
})
export class RoomEditComponent extends AbstractEditorComponent<RoomResponse, RoomRequest> {
  private readonly roomForm = viewChild(RoomFormComponent);

  protected override entityForm(): FormGroup | undefined {
    return this.roomForm()?.form;
  }
}
