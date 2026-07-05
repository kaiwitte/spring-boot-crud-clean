import { Component, viewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';

import { RoomRequest, RoomResponse } from '@generated';

import { AbstractCreatorComponent } from '../shared/abstract-creator/abstract-creator.component';
import { RoomFormComponent } from './room-form.component';

@Component({
  selector: 'app-room-create',
  imports: [MatCardModule, RoomFormComponent],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>New {{ config.names.singular }}</mat-card-title>
      </mat-card-header>
      <mat-card-content class="pt-4">
        <app-room-form
          [pending]="pending()"
          [generalErrors]="generalErrors()"
          [cancelLink]="listLink"
          submitLabel="Create"
          (save)="submit($event)"
        />
      </mat-card-content>
    </mat-card>
  `,
})
export class RoomCreateComponent extends AbstractCreatorComponent<RoomResponse, RoomRequest> {
  private readonly roomForm = viewChild(RoomFormComponent);

  protected override entityForm(): FormGroup | undefined {
    return this.roomForm()?.form;
  }
}
