import { InjectionToken, Provider, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ServerPageRequest, ServerPageResponse } from '../abstract-table/table-model';

/**
 * Contract every concrete model feature fulfils by adapting its generated
 * OpenAPI service. The generated request/response DTOs stay the source of
 * truth; this interface only normalizes the call surface for the shared
 * abstract-* building blocks.
 */
export interface CrudResource<TResponse, TRequest, TId = string> {
  list(): Observable<readonly TResponse[]>;
  /**
   * Server-side paged, sorted and filtered list. When implemented, list
   * pages prefer it over paging the full list() result client-side.
   */
  listPage?(request: ServerPageRequest): Observable<ServerPageResponse<TResponse>>;
  get(id: TId): Observable<TResponse>;
  create(request: TRequest): Observable<TResponse>;
  update(id: TId, request: TRequest): Observable<TResponse>;
  delete(id: TId): Observable<unknown>;
  id(item: TResponse): TId;
  label(item: TResponse): string;
}

/** Router command factories so the abstract layer never hardcodes URLs. */
export interface CrudRoutes<TId = string> {
  list(): readonly string[];
  detail(id: TId): readonly string[];
  create(): readonly string[];
  edit(id: TId): readonly string[];
}

export interface CrudNames {
  readonly singular: string;
  readonly plural: string;
}

export interface CrudResourceConfig<TResponse, TRequest, TId = string> {
  readonly resource: CrudResource<TResponse, TRequest, TId>;
  readonly names: CrudNames;
  readonly routes: CrudRoutes<TId>;
}

export const CRUD_RESOURCE = new InjectionToken<CrudResourceConfig<unknown, unknown, never>>(
  'CRUD_RESOURCE',
);

/** Provide a concrete resource config at route level under the shared token. */
export function provideCrudResource<TResponse, TRequest, TId>(
  factory: () => CrudResourceConfig<TResponse, TRequest, TId>,
): Provider {
  return { provide: CRUD_RESOURCE, useFactory: factory };
}

/** Typed accessor for the route-provided resource config. */
export function injectCrudResource<TResponse, TRequest, TId = string>(): CrudResourceConfig<
  TResponse,
  TRequest,
  TId
> {
  return inject(CRUD_RESOURCE) as unknown as CrudResourceConfig<TResponse, TRequest, TId>;
}
