import { Component, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import {
  BehaviorSubject,
  Subject,
  catchError,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  of,
  switchMap,
  tap,
} from 'rxjs';

import { errorMessage } from '../api/api-error';
import {
  TableColumn,
  TableDataProvider,
  TablePage,
  TableQuery,
  TableRowAction,
} from './table-model';

const ACTIONS_COLUMN = 'actions';
const FILTER_DEBOUNCE_MS = 200;

const EMPTY_PAGE: TablePage<never> = { rows: [], total: 0 };

/**
 * Generic list workflow: renders any model type from typed column
 * definitions, delegates data access to a TableDataProvider and owns
 * paging, sorting, filtering, loading and error states.
 */
@Component({
  selector: 'app-abstract-table',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSortModule,
    MatTableModule,
    MatTooltipModule,
    RouterLink,
  ],
  templateUrl: './abstract-table.component.html',
})
export class AbstractTableComponent<T> {
  readonly title = input('');
  readonly columns = input.required<readonly TableColumn<T>[]>();
  readonly provider = input.required<TableDataProvider<T>>();
  readonly actions = input<readonly TableRowAction<T>[]>([]);
  /** Router commands for a row click, e.g. the detail view. */
  readonly rowLink = input<((row: T) => readonly string[]) | null>(null);
  readonly createLink = input<readonly string[] | null>(null);
  readonly createLabel = input('New');
  readonly pageSizeOptions = input<readonly number[]>([5, 10, 25]);

  private readonly router = inject(Router);

  private readonly query = signal<TableQuery>({
    pageIndex: 0,
    pageSize: 10,
    sort: null,
    filter: '',
  });
  private readonly filterInput$ = new Subject<string>();
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly page = toSignal(
    combineLatest([toObservable(this.provider), toObservable(this.query), this.refresh$]).pipe(
      tap(() => {
        this.loading.set(true);
        this.error.set(null);
      }),
      switchMap(([provider, query]) =>
        provider.fetch(query).pipe(
          // keep the outer stream alive on errors so retrying stays possible
          catchError((error: unknown) => {
            this.error.set(errorMessage(error, 'Loading data failed.'));
            return of(EMPTY_PAGE as TablePage<T>);
          }),
          tap(() => this.loading.set(false)),
        ),
      ),
    ),
    { initialValue: EMPTY_PAGE as TablePage<T> },
  );

  protected readonly rows = computed(() => [...this.page().rows]);
  protected readonly pageIndex = computed(() => this.query().pageIndex);
  protected readonly pageSize = computed(() => this.query().pageSize);
  protected readonly displayedColumns = computed(() => {
    const keys = this.columns().map((column) => column.key);
    return this.actions().length > 0 ? [...keys, ACTIONS_COLUMN] : keys;
  });
  protected readonly actionsColumn = ACTIONS_COLUMN;

  constructor() {
    this.filterInput$
      .pipe(debounceTime(FILTER_DEBOUNCE_MS), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((filter) => this.updateQuery({ filter, pageIndex: 0 }));
  }

  /** Re-runs the current query, e.g. after a row was deleted. */
  refresh(): void {
    this.refresh$.next(undefined);
  }

  protected onFilterInput(value: string): void {
    this.filterInput$.next(value);
  }

  protected onPage(event: PageEvent): void {
    this.updateQuery({ pageIndex: event.pageIndex, pageSize: event.pageSize });
  }

  protected onSort(sort: Sort): void {
    const tableSort =
      sort.direction === '' ? null : { field: sort.active, direction: sort.direction };
    this.updateQuery({ pageIndex: 0, sort: tableSort });
  }

  protected onRowClick(row: T): void {
    const link = this.rowLink();
    if (link !== null) {
      void this.router.navigate([...link(row)]);
    }
  }

  private updateQuery(patch: Partial<TableQuery>): void {
    this.query.update((query) => ({ ...query, ...patch }));
  }
}
