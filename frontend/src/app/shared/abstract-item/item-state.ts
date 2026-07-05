import { HttpErrorResponse } from '@angular/common/http';
import { Signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { catchError, map, of, startWith, switchMap } from 'rxjs';

import { errorMessage } from '../api/api-error';
import { CrudResourceConfig } from '../crud/crud-resource';
import { ItemState } from './item-model';

/**
 * Loads the item identified by the current route through the generated API
 * (via the CrudResource adapter) and exposes it as loading/not-found/error/
 * loaded state. Only a 404 counts as not-found; any other failure carries
 * its message into the error state. Must be called in an injection context.
 */
export function crudItemState<TResponse, TRequest>(
  config: CrudResourceConfig<TResponse, TRequest>,
  route: ActivatedRoute,
  idParam = 'id',
): Signal<ItemState<TResponse>> {
  const loading: ItemState<TResponse> = { kind: 'loading' };
  const notFound: ItemState<TResponse> = { kind: 'not-found' };

  return toSignal(
    route.paramMap.pipe(
      map((params) => params.get(idParam)),
      switchMap((id) => {
        if (id === null) {
          return of(notFound);
        }
        return config.resource.get(id).pipe(
          map((item): ItemState<TResponse> => ({ kind: 'loaded', item })),
          startWith(loading),
          catchError((error: unknown) => of(toFailureState<TResponse>(error, config.names.singular))),
        );
      }),
    ),
    { initialValue: loading },
  );
}

function toFailureState<TResponse>(error: unknown, entityName: string): ItemState<TResponse> {
  if (error instanceof HttpErrorResponse && error.status === 404) {
    return { kind: 'not-found' };
  }
  return {
    kind: 'error',
    message: errorMessage(error, `Loading ${entityName} failed.`),
  };
}
