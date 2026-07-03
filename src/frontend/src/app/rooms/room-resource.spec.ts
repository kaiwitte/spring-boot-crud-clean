import { firstValueFrom, of } from 'rxjs';
import { instance, mock, verify, when } from 'ts-mockito';

import { RoomRequest, RoomResponse, RoomService } from '@generated';

import { RoomCrudResource, roomResourceConfig } from './room-resource';

const room: RoomResponse = { id: 'room-1', name: 'Blue Room' };
const request: RoomRequest = { name: 'Blue Room' };

describe('RoomCrudResource', () => {
  let roomService: RoomService;
  let resource: RoomCrudResource;

  beforeEach(() => {
    roomService = mock(RoomService);
    resource = new RoomCrudResource(instance(roomService));
  });

  it('lists rooms by unwrapping the generated list response', async () => {
    when(roomService.listRoom()).thenReturn(of({ results: [room] }));

    await expect(firstValueFrom(resource.list())).resolves.toEqual([room]);
  });

  it('lists an empty array when the generated response has no results', async () => {
    when(roomService.listRoom()).thenReturn(of({}));

    await expect(firstValueFrom(resource.list())).resolves.toEqual([]);
  });

  it('delegates get to the generated getRoom call', async () => {
    when(roomService.getRoom('room-1')).thenReturn(of(room));

    await expect(firstValueFrom(resource.get('room-1'))).resolves.toEqual(room);
  });

  it('delegates create to the generated newRoom call', async () => {
    when(roomService.newRoom(request)).thenReturn(of(room));

    await expect(firstValueFrom(resource.create(request))).resolves.toEqual(room);
  });

  it('delegates update to the generated updateRoom call', async () => {
    when(roomService.updateRoom('room-1', request)).thenReturn(of(room));

    await expect(firstValueFrom(resource.update('room-1', request))).resolves.toEqual(room);
  });

  it('delegates delete to the generated deleteRoom call', async () => {
    when(roomService.deleteRoom('room-1')).thenReturn(of(undefined));

    await firstValueFrom(resource.delete('room-1'), { defaultValue: undefined });

    verify(roomService.deleteRoom('room-1')).once();
  });

  it('exposes the generated id and a human-readable label', () => {
    expect(resource.id(room)).toBe('room-1');
    expect(resource.label(room)).toBe('Blue Room');
  });
});

describe('roomResourceConfig', () => {
  it('builds router commands from generated ids', () => {
    const config = roomResourceConfig(instance(mock(RoomService)));

    expect(config.routes.list()).toEqual(['/rooms']);
    expect(config.routes.detail('room-1')).toEqual(['/rooms', 'room-1']);
    expect(config.routes.create()).toEqual(['/rooms', 'new']);
    expect(config.routes.edit('room-1')).toEqual(['/rooms', 'room-1', 'edit']);
    expect(config.names).toEqual({ singular: 'Room', plural: 'Rooms' });
  });
});
