import { Directive, computed, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { DeleteFlowService } from '../abstract-delete-dialog/delete-flow.service';
import { injectCrudResource } from '../crud/crud-resource';
import { crudItemState } from './item-state';

/**
 * Base workflow for detail pages: loads the routed item through the
 * generated API, exposes edit/back links and runs the shared delete flow
 * (navigating back to the list after a successful delete).
 */
@Directive()
export abstract class AbstractDetailPageComponent<TResponse, TRequest> {
  protected readonly config = injectCrudResource<TResponse, TRequest>();
  private readonly router = inject(Router);
  private readonly deleteFlow = inject(DeleteFlowService);

  protected readonly state = crudItemState(this.config, inject(ActivatedRoute));

  protected readonly item = computed<TResponse | null>(() => {
    const state = this.state();
    return state.kind === 'loaded' ? state.item : null;
  });

  protected readonly listLink: string[] = [...this.config.routes.list()];

  protected readonly editLink = computed<readonly string[] | null>(() => {
    const item = this.item();
    return item === null ? null : this.config.routes.edit(this.config.resource.id(item));
  });

  protected delete(): void {
    const item = this.item();
    if (item === null) {
      return;
    }
    this.deleteFlow
      .confirm({
        entityName: this.config.names.singular,
        itemLabel: this.config.resource.label(item),
        performDelete: () => this.config.resource.delete(this.config.resource.id(item)),
      })
      .subscribe((deleted) => {
        if (deleted) {
          void this.router.navigate(this.listLink);
        }
      });
  }
}
