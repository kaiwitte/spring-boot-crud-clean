import { firstValueFrom, of } from 'rxjs';

import { ClientSideTableProvider } from './client-side-table-provider';
import { TableColumn, TableQuery } from './table-model';

interface TestRow {
  readonly id: string;
  readonly name: string;
  readonly size: number;
}

const columns: readonly TableColumn<TestRow>[] = [
  { key: 'name', header: 'Name', value: (row) => row.name },
  { key: 'size', header: 'Size', value: (row) => row.size },
];

const rows: readonly TestRow[] = [
  { id: '1', name: 'Charlie', size: 30 },
  { id: '2', name: 'alpha', size: 10 },
  { id: '3', name: 'Bravo', size: 20 },
  { id: '4', name: 'delta', size: 5 },
];

const query = (overrides: Partial<TableQuery> = {}): TableQuery => ({
  pageIndex: 0,
  pageSize: 10,
  sort: null,
  filter: '',
  ...overrides,
});

const fetchPage = (tableQuery: TableQuery) => {
  const provider = new ClientSideTableProvider<TestRow>(() => of(rows), columns);
  return firstValueFrom(provider.fetch(tableQuery));
};

describe('ClientSideTableProvider', () => {
  it('returns all rows and the full total for an empty query', async () => {
    const page = await fetchPage(query());

    expect(page.rows).toEqual(rows);
    expect(page.total).toBe(4);
  });

  it('filters case-insensitively across all configured columns', async () => {
    const page = await fetchPage(query({ filter: 'ALP' }));

    expect(page.rows.map((row) => row.name)).toEqual(['alpha']);
    expect(page.total).toBe(1);
  });

  it('matches the filter against numeric column values', async () => {
    const page = await fetchPage(query({ filter: '20' }));

    expect(page.rows.map((row) => row.name)).toEqual(['Bravo']);
  });

  it('sorts ascending case-insensitively by the requested column', async () => {
    const page = await fetchPage(query({ sort: { field: 'name', direction: 'asc' } }));

    expect(page.rows.map((row) => row.name)).toEqual(['alpha', 'Bravo', 'Charlie', 'delta']);
  });

  it('sorts descending when requested', async () => {
    const page = await fetchPage(query({ sort: { field: 'name', direction: 'desc' } }));

    expect(page.rows.map((row) => row.name)).toEqual(['delta', 'Charlie', 'Bravo', 'alpha']);
  });

  it('sorts numeric columns numerically, not lexicographically', async () => {
    const page = await fetchPage(query({ sort: { field: 'size', direction: 'asc' } }));

    expect(page.rows.map((row) => row.size)).toEqual([5, 10, 20, 30]);
  });

  it('ignores a sort field that matches no column', async () => {
    const page = await fetchPage(query({ sort: { field: 'unknown', direction: 'asc' } }));

    expect(page.rows).toEqual(rows);
  });

  it('applies paging after filtering and sorting and keeps the filtered total', async () => {
    const page = await fetchPage(
      query({ pageIndex: 1, pageSize: 2, sort: { field: 'size', direction: 'asc' } }),
    );

    expect(page.rows.map((row) => row.size)).toEqual([20, 30]);
    expect(page.total).toBe(4);
  });

  it('returns an empty page when the page index is out of range', async () => {
    const page = await fetchPage(query({ pageIndex: 5, pageSize: 2 }));

    expect(page.rows).toEqual([]);
    expect(page.total).toBe(4);
  });
});
