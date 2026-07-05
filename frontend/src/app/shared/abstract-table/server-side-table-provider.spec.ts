import { Observable, firstValueFrom, of } from 'rxjs';

import { ServerSideTableProvider } from './server-side-table-provider';
import { ServerPageRequest, ServerPageResponse, TableQuery } from './table-model';

interface TestRow {
  readonly id: string;
  readonly name: string;
}

const rows: readonly TestRow[] = [
  { id: '1', name: 'alpha' },
  { id: '2', name: 'bravo' },
];

const query = (overrides: Partial<TableQuery> = {}): TableQuery => ({
  pageIndex: 0,
  pageSize: 10,
  sort: null,
  filter: '',
  ...overrides,
});

/** Captures the request passed to the server call and answers with a canned response. */
const capturingProvider = (response: ServerPageResponse<TestRow>) => {
  const requests: ServerPageRequest[] = [];
  const provider = new ServerSideTableProvider<TestRow>(
    (request): Observable<ServerPageResponse<TestRow>> => {
      requests.push(request);
      return of(response);
    },
  );
  return { provider, requests };
};

describe('ServerSideTableProvider', () => {
  it('passes paging through and maps results and total from the response', async () => {
    const { provider, requests } = capturingProvider({
      pagination: { total: 42 },
      results: [...rows],
    });

    const page = await firstValueFrom(provider.fetch(query({ pageIndex: 2, pageSize: 5 })));

    expect(requests).toEqual([
      {
        pageIndex: 2,
        pageSize: 5,
        sortDirection: undefined,
        sortField: undefined,
        filter: undefined,
      },
    ]);
    expect(page.rows).toEqual(rows);
    expect(page.total).toBe(42);
  });

  it('maps an ascending table sort to the server ASC direction and field', async () => {
    const { provider, requests } = capturingProvider({ results: [] });

    await firstValueFrom(provider.fetch(query({ sort: { field: 'name', direction: 'asc' } })));

    expect(requests[0].sortDirection).toBe('ASC');
    expect(requests[0].sortField).toBe('name');
  });

  it('maps a descending table sort to the server DESC direction and field', async () => {
    const { provider, requests } = capturingProvider({ results: [] });

    await firstValueFrom(provider.fetch(query({ sort: { field: 'name', direction: 'desc' } })));

    expect(requests[0].sortDirection).toBe('DESC');
    expect(requests[0].sortField).toBe('name');
  });

  it('sends the trimmed filter', async () => {
    const { provider, requests } = capturingProvider({ results: [] });

    await firstValueFrom(provider.fetch(query({ filter: '  blue  ' })));

    expect(requests[0].filter).toBe('blue');
  });

  it('omits a blank filter instead of sending whitespace', async () => {
    const { provider, requests } = capturingProvider({ results: [] });

    await firstValueFrom(provider.fetch(query({ filter: '   ' })));

    expect(requests[0].filter).toBeUndefined();
  });

  it('falls back to an empty page when the response carries no results', async () => {
    const { provider } = capturingProvider({});

    const page = await firstValueFrom(provider.fetch(query()));

    expect(page.rows).toEqual([]);
    expect(page.total).toBe(0);
  });

  it('falls back to the row count when the response carries no pagination', async () => {
    const { provider } = capturingProvider({ results: [...rows] });

    const page = await firstValueFrom(provider.fetch(query()));

    expect(page.total).toBe(2);
  });
});
