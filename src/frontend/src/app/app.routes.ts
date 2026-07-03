import { Routes } from '@angular/router';

import { NotFoundComponent } from './shared/not-found/not-found.component';

export const routes: Routes = [
  { path: '', redirectTo: 'rooms', pathMatch: 'full' },
  {
    path: 'rooms',
    loadChildren: () => import('./rooms/room.routes').then((m) => m.ROOM_ROUTES),
  },
  { path: '**', component: NotFoundComponent },
];
