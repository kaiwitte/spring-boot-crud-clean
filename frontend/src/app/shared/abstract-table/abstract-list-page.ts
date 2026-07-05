import { Directive, inject, viewChild } from '@angular/core';
import { Router } from '@angular/router';

import { DeleteFlowService } from '../abstract-delete-dialog/delete-flow.service';
import { injectCrudResource } from '../crud/crud-resource';
import { AbstractTableComponent } from './abstract-table.component';
import { ServerSideTableProvider } from './server-side-table-provider';
import { TableColumn, TableRowAction } from './table-model';

/**
 * Base workflow for list pages: wires the route-provided CrudResource into
 * the abstract table, with view/edit navigation and the shared delete flow.
 * Concrete pages only supply typed column definitions.
 */
@Directive()
export abstract class AbstractListPageComponent<TResponse, TRequest> {
  protected readonly config = injectCrudResource<TResponse, TRequest>();
  private readonly router = inject(Router);
  private readonly deleteFlow = inject(DeleteFlowService);
  private readonly table = viewChild(AbstractTableComponent);

  protected abstract readonly columns: readonly TableColumn<TResponse>[];

  protected readonly createLink: string[] = [...this.config.routes.create()];

  protected readonly actions: readonly TableRowAction<TResponse>[] = [
    {
      icon: 'edit',
      label: 'Edit',
      run: (row) => void this.router.navigate([...this.config.routes.edit(this.id(row))]),
    },
    {
      icon: 'delete',
      label: 'Delete',
      run: (row) => this.confirmDelete(row),
    },
  ];

  protected readonly rowLink = (row: TResponse): readonly string[] =>
    this.config.routes.detail(this.id(row));

  protected readonly provider = new ServerSideTableProvider<TResponse>((request) =>
    this.config.resource.listPage(request),
  );

  protected confirmDelete(row: TResponse): void {
    this.deleteFlow
      .confirm({
        entityName: this.config.names.singular,
        itemLabel: this.config.resource.label(row),
        performDelete: () => this.config.resource.delete(this.id(row)),
      })
      .subscribe((deleted) => {
        if (deleted) {
          this.table()?.refresh();
        }
      });
  }

  private id(row: TResponse): string {
    return this.config.resource.id(row);
  }
}
