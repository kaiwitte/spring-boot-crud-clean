import { Component } from '@angular/core';

import { RoomRequest, RoomResponse } from '@generated';

import { AbstractListPageComponent } from '../shared/abstract-table/abstract-list-page';
import { AbstractTableComponent } from '../shared/abstract-table/abstract-table.component';
import { TableColumn } from '../shared/abstract-table/table-model';

@Component({
  selector: 'app-room-table',
  imports: [AbstractTableComponent],
  template: `
    <app-abstract-table
      [title]="config.names.plural"
      [columns]="columns"
      [provider]="provider"
      [actions]="actions"
      [rowLink]="rowLink"
      [createLink]="createLink"
      [createLabel]="'New ' + config.names.singular"
    />
  `,
})
export class RoomTableComponent extends AbstractListPageComponent<RoomResponse, RoomRequest> {
  protected override readonly columns: readonly TableColumn<RoomResponse>[] = [
    { key: 'name', header: 'Name', value: (room) => room.name },
    { key: 'id', header: 'ID', value: (room) => room.id, sortable: false },
  ];
}
