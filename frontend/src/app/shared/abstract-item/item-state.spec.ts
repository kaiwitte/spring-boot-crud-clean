import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { NEVER, Observable, of, throwError } from 'rxjs';

import { CrudResourceConfig } from '../crud/crud-resource';
import { crudItemState } from './item-state';

interface TestItem {
  readonly id: string;
  readonly name: string;
}

const item: TestItem = { id: 'item-1', name: 'Blue Room' };

const notCalled = (): never => {
  throw new Error('not expected to be called');
};

const configWith = (get: () => Observable<TestItem>): CrudResourceConfig<TestItem, unknown> => ({
  resource: {
    listPage: notCalled,
    get,
    create: notCalled,
    update: notCalled,
    delete: notCalled,
    id: (candidate) => candidate.id,
    label: (candidate) => candidate.name,
  },
  names: { singular: 'Room', plural: 'Rooms' },
  routes: {
    list: () => ['/rooms'],
    detail: (id) => ['/rooms', id],
    create: () => ['/rooms', 'new'],
    edit: (id) => ['/rooms', id, 'edit'],
  },
});

const routeWithId = (id: string): ActivatedRoute =>
  ({ paramMap: of(convertToParamMap({ id })) }) as unknown as ActivatedRoute;

const stateFor = (get: () => Observable<TestItem>) =>
  TestBed.runInInjectionContext(() => crudItemState(configWith(get), routeWithId('item-1')));

describe('crudItemState', () => {
  it('stays in loading state while the item request is pending', () => {
    const state = stateFor(() => NEVER);

    expect(state()).toEqual({ kind: 'loading' });
  });

  it('exposes the loaded item', () => {
    const state = stateFor(() => of(item));

    expect(state()).toEqual({ kind: 'loaded', item });
  });

  it('maps a 404 response to not-found', () => {
    const state = stateFor(() => throwError(() => new HttpErrorResponse({ status: 404 })));

    expect(state()).toEqual({ kind: 'not-found' });
  });

  it('maps a non-404 failure to an error state with the structured message', () => {
    const httpError = new HttpErrorResponse({
      status: 500,
      error: { message: 'Database unavailable', errorType: 'INTERNAL_SERVER_ERROR' },
    });
    const state = stateFor(() => throwError(() => httpError));

    expect(state()).toEqual({ kind: 'error', message: 'Database unavailable' });
  });

  it('maps a network failure to an error state with a fallback message', () => {
    const state = stateFor(() => throwError(() => new HttpErrorResponse({ status: 0 })));

    expect(state()).toEqual({ kind: 'error', message: 'Loading Room failed.' });
  });
});
