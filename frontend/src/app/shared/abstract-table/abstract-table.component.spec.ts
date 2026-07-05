import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { AbstractTableComponent } from './abstract-table.component';
import { TableColumn, TableDataProvider } from './table-model';

interface TestRow {
  readonly id: string;
  readonly name: string;
}

const rows: readonly TestRow[] = [{ id: 'row-1', name: 'Blue Room' }];

const provider: TableDataProvider<TestRow> = {
  fetch: () => of({ rows, total: rows.length }),
};

const columns: readonly TableColumn<TestRow>[] = [
  { key: 'name', header: 'Name', value: (row) => row.name },
  { key: 'id', header: 'ID', value: (row) => row.id },
];

const createTable = async (
  rowLink: ((row: TestRow) => readonly string[]) | null,
): Promise<ComponentFixture<AbstractTableComponent<TestRow>>> => {
  await TestBed.configureTestingModule({
    imports: [AbstractTableComponent],
    providers: [provideRouter([])],
  }).compileComponents();

  const fixture = TestBed.createComponent(AbstractTableComponent<TestRow>);
  fixture.componentRef.setInput('columns', columns);
  fixture.componentRef.setInput('provider', provider);
  fixture.componentRef.setInput('rowLink', rowLink);
  fixture.detectChanges();
  return fixture;
};

describe('AbstractTableComponent', () => {
  it('renders the first column cell as a link to the row target', async () => {
    const fixture = await createTable((row) => ['/rooms', row.id]);

    const element = fixture.nativeElement as HTMLElement;
    const link = element.querySelector<HTMLAnchorElement>('td a[href="/rooms/row-1"]');
    expect(link?.textContent).toContain('Blue Room');
  });

  it('keeps the remaining columns as plain text', async () => {
    const fixture = await createTable((row) => ['/rooms', row.id]);

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('td a').length).toBe(1);
  });

  it('renders all cells as plain text without a row link', async () => {
    const fixture = await createTable(null);

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('td a').length).toBe(0);
    expect(element.textContent).toContain('Blue Room');
  });
});
