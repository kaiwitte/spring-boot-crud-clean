import { Observable, map } from 'rxjs';

import {
  ServerPageRequest,
  ServerPageResponse,
  TableDataProvider,
  TablePage,
  TableQuery,
} from './table-model';

/**
 * Fulfils the TableDataProvider contract by delegating paging, sorting and
 * filtering to a generated list call that accepts the server-side parameters.
 */
export class ServerSideTableProvider<T> implements TableDataProvider<T> {
  constructor(
    private readonly loadPage: (request: ServerPageRequest) => Observable<ServerPageResponse<T>>,
  ) {}

  fetch(query: TableQuery): Observable<TablePage<T>> {
    return this.loadPage(toServerPageRequest(query)).pipe(map((response) => toTablePage(response)));
  }
}

function toServerPageRequest(query: TableQuery): ServerPageRequest {
  const filter = query.filter.trim();
  return {
    pageIndex: query.pageIndex,
    pageSize: query.pageSize,
    sortDirection:
      query.sort === null ? undefined : query.sort.direction === 'asc' ? 'ASC' : 'DESC',
    sortField: query.sort?.field,
    filter: filter === '' ? undefined : filter,
  };
}

function toTablePage<T>(response: ServerPageResponse<T>): TablePage<T> {
  const rows = response.results ?? [];
  return { rows, total: response.pagination?.total ?? rows.length };
}
