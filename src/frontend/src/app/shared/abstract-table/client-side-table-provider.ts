import { Observable, map } from 'rxjs';

import {
  TableCellValue,
  TableColumn,
  TableDataProvider,
  TablePage,
  TableQuery,
} from './table-model';

/**
 * Fulfils the TableDataProvider contract on top of a generated list call
 * that returns the full result set: filtering, sorting and paging happen
 * client-side against the configured columns.
 */
export class ClientSideTableProvider<T> implements TableDataProvider<T> {
  constructor(
    private readonly loadAll: () => Observable<readonly T[]>,
    private readonly columns: readonly TableColumn<T>[],
  ) {}

  fetch(query: TableQuery): Observable<TablePage<T>> {
    return this.loadAll().pipe(map((rows) => this.applyQuery(rows, query)));
  }

  private applyQuery(rows: readonly T[], query: TableQuery): TablePage<T> {
    const filtered = this.filter(rows, query.filter);
    const sorted = this.sort(filtered, query.sort);
    const start = query.pageIndex * query.pageSize;
    return {
      rows: sorted.slice(start, start + query.pageSize),
      total: filtered.length,
    };
  }

  private filter(rows: readonly T[], filter: string): readonly T[] {
    const needle = filter.trim().toLowerCase();
    if (needle === '') {
      return rows;
    }
    return rows.filter((row) =>
      this.columns.some((column) => text(column.value(row)).toLowerCase().includes(needle)),
    );
  }

  private sort(rows: readonly T[], sort: TableQuery['sort']): readonly T[] {
    if (sort === null) {
      return rows;
    }
    const column = this.columns.find((candidate) => candidate.key === sort.field);
    if (column === undefined) {
      return rows;
    }
    const factor = sort.direction === 'asc' ? 1 : -1;
    return [...rows].sort(
      (left, right) => factor * compare(column.value(left), column.value(right)),
    );
  }
}

function compare(left: TableCellValue, right: TableCellValue): number {
  if (typeof left === 'number' && typeof right === 'number') {
    return left - right;
  }
  return text(left).localeCompare(text(right), undefined, { sensitivity: 'base' });
}

function text(value: TableCellValue): string {
  return value === null || value === undefined ? '' : String(value);
}
