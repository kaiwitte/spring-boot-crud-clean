import { Routes } from '@angular/router';

import { RoomCreateComponent } from './room-create.component';
import { RoomDetailComponent } from './room-detail.component';
import { RoomEditComponent } from './room-edit.component';
import { RoomTableComponent } from './room-table.component';
import { provideRoomResource } from './room-resource';

export const ROOM_ROUTES: Routes = [
  {
    path: '',
    providers: [provideRoomResource()],
    children: [
      { path: '', component: RoomTableComponent },
      { path: 'new', component: RoomCreateComponent },
      { path: ':id', component: RoomDetailComponent },
      { path: ':id/edit', component: RoomEditComponent },
    ],
  },
];
