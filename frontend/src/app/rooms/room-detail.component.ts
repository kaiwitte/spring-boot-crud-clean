import { Component } from '@angular/core';

import { RoomRequest, RoomResponse } from '@generated';

import { AbstractDetailPageComponent } from '../shared/abstract-item/abstract-detail-page';
import { AbstractItemComponent } from '../shared/abstract-item/abstract-item.component';
import { ItemField } from '../shared/abstract-item/item-model';

@Component({
  selector: 'app-room-detail',
  imports: [AbstractItemComponent],
  template: `
    <app-abstract-item
      [state]="state()"
      [entityName]="config.names.singular"
      [fields]="fields"
      [title]="titleOf"
      [editLink]="editLink()"
      [backLink]="listLink"
      (deleteRequested)="delete()"
    />
  `,
})
export class RoomDetailComponent extends AbstractDetailPageComponent<RoomResponse, RoomRequest> {
  protected readonly fields: readonly ItemField<RoomResponse>[] = [
    { label: 'ID', value: (room) => room.id },
    { label: 'Name', value: (room) => room.name },
  ];

  protected readonly titleOf = (room: RoomResponse): string => room.name;
}
