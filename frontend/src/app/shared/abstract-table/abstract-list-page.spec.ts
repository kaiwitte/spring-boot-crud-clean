import { Observable, firstValueFrom, of } from 'rxjs';

import { CrudResource } from '../crud/crud-resource';
import { tableProviderFor } from './abstract-list-page';
import { ClientSideTableProvider } from './client-side-table-provider';
import { ServerSideTableProvider } from './server-side-table-provider';
import { ServerPageResponse, TableColumn, TableQuery } from './table-model';

interface TestRow {
  readonly id: string;
  readonly name: string;
}

const columns: readonly TableColumn<TestRow>[] = [
  { key: 'name', header: 'Name', value: (row) => row.name },
];

const query: TableQuery = { pageIndex: 0, pageSize: 10, sort: null, filter: '' };

const notCalled = (): never => {
  throw new Error('not expected to be called');
};

const clientOnlyResource: CrudResource<TestRow, unknown> = {
  list: () => of([{ id: '1', name: 'alpha' }]),
  get: notCalled,
  create: notCalled,
  update: notCalled,
  delete: notCalled,
  id: (item) => item.id,
  label: (item) => item.name,
};

/** Uses instance state in listPage so the tests catch a lost `this` binding. */
class ServerResource implements CrudResource<TestRow, unknown> {
  private readonly rows: readonly TestRow[] = [{ id: '1', name: 'alpha' }];

  list = notCalled;
  get = notCalled;
  create = notCalled;
  update = notCalled;
  delete = notCalled;

  listPage(): Observable<ServerPageResponse<TestRow>> {
    return of({ pagination: { total: this.rows.length }, results: this.rows });
  }

  id(item: TestRow): string {
    return item.id;
  }

  label(item: TestRow): string {
    return item.name;
  }
}

describe('tableProviderFor', () => {
  it('prefers the server-side provider when the resource implements listPage', () => {
    expect(tableProviderFor(new ServerResource(), columns)).toBeInstanceOf(ServerSideTableProvider);
  });

  it('falls back to the client-side provider when the resource has no listPage', () => {
    expect(tableProviderFor(clientOnlyResource, columns)).toBeInstanceOf(ClientSideTableProvider);
  });

  it('keeps the resource as receiver when delegating to listPage', async () => {
    const provider = tableProviderFor(new ServerResource(), columns);

    const page = await firstValueFrom(provider.fetch(query));

    expect(page.rows.map((row) => row.name)).toEqual(['alpha']);
    expect(page.total).toBe(1);
  });
});
