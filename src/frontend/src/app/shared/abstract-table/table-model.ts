import { Observable } from 'rxjs';

export type TableSortDirection = 'asc' | 'desc';

export interface TableSort {
  readonly field: string;
  readonly direction: TableSortDirection;
}

/** Everything a data provider needs to produce one page of rows. */
export interface TableQuery {
  readonly pageIndex: number;
  readonly pageSize: number;
  readonly sort: TableSort | null;
  readonly filter: string;
}

export interface TablePage<T> {
  readonly rows: readonly T[];
  /** Total matching rows across all pages (after filtering). */
  readonly total: number;
}

/**
 * Generic table service contract. Implementations may resolve the query
 * client-side (see ClientSideTableProvider) or delegate paging/sorting to
 * the server once the generated API exposes those parameters.
 */
export interface TableDataProvider<T> {
  fetch(query: TableQuery): Observable<TablePage<T>>;
}

export type TableCellValue = string | number | boolean | null | undefined;

/** Typed column configuration supplied by the concrete feature layer. */
export interface TableColumn<T> {
  /** Column id; also used as the sort field passed back in TableQuery. */
  readonly key: string;
  readonly header: string;
  readonly value: (row: T) => TableCellValue;
  /** Defaults to true. */
  readonly sortable?: boolean;
}

/** Typed per-row action configuration supplied by the concrete feature layer. */
export interface TableRowAction<T> {
  /** Material icon name. */
  readonly icon: string;
  readonly label: string;
  readonly run: (row: T) => void;
}
