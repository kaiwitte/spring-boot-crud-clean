import { Provider, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { RoomRequest, RoomResponse, RoomService } from '@generated';

import { ServerPageRequest, ServerPageResponse } from '../shared/abstract-table/table-model';
import {
  CrudResource,
  CrudResourceConfig,
  provideCrudResource,
} from '../shared/crud/crud-resource';

/** Adapts the generated RoomService to the generic CrudResource contract. */
export class RoomCrudResource implements CrudResource<RoomResponse, RoomRequest> {
  constructor(private readonly roomService: RoomService) {}

  list(): Observable<readonly RoomResponse[]> {
    return this.roomService.listRoom().pipe(map((response) => response.results ?? []));
  }

  listPage(request: ServerPageRequest): Observable<ServerPageResponse<RoomResponse>> {
    const sort =
      request.sortField === undefined
        ? undefined
        : [`${request.sortField},${request.sortDirection ?? 'ASC'}`];
    return this.roomService.listRoom(request.pageIndex, request.pageSize, sort, request.filter);
  }

  get(id: string): Observable<RoomResponse> {
    return this.roomService.getRoom(id);
  }

  create(request: RoomRequest): Observable<RoomResponse> {
    return this.roomService.newRoom(request);
  }

  update(id: string, request: RoomRequest): Observable<RoomResponse> {
    return this.roomService.updateRoom(id, request);
  }

  delete(id: string): Observable<unknown> {
    return this.roomService.deleteRoom(id);
  }

  id(item: RoomResponse): string {
    return item.id;
  }

  label(item: RoomResponse): string {
    return item.name;
  }
}

export function roomResourceConfig(
  roomService: RoomService,
): CrudResourceConfig<RoomResponse, RoomRequest> {
  return {
    resource: new RoomCrudResource(roomService),
    names: { singular: 'Room', plural: 'Rooms' },
    routes: {
      list: () => ['/rooms'],
      detail: (id) => ['/rooms', id],
      create: () => ['/rooms', 'new'],
      edit: (id) => ['/rooms', id, 'edit'],
    },
  };
}

/** Route-level provider wiring the Room feature into the shared CRUD token. */
export function provideRoomResource(): Provider {
  return provideCrudResource(() => roomResourceConfig(inject(RoomService)));
}
