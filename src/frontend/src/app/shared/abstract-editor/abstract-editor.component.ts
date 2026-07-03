import { Directive, computed, inject } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';

import { ServerFieldMap } from '../abstract-form/server-validation';
import { crudItemState } from '../abstract-item/item-state';
import { injectCrudResource } from '../crud/crud-resource';
import { CrudSaveFlow } from '../crud/crud-save-flow';

/**
 * Generic edit flow: loads the routed item through the generated API,
 * feeds it into the concrete form and submits via the generated update
 * call, mapping server validation errors back onto the form.
 */
@Directive()
export abstract class AbstractEditorComponent<TResponse, TRequest> {
  protected readonly config = injectCrudResource<TResponse, TRequest>();
  private readonly router = inject(Router);

  protected readonly state = crudItemState(this.config, inject(ActivatedRoute));

  protected readonly item = computed<TResponse | null>(() => {
    const state = this.state();
    return state.kind === 'loaded' ? state.item : null;
  });

  protected readonly listLink: string[] = [...this.config.routes.list()];

  protected readonly cancelLink = computed<readonly string[]>(() => {
    const item = this.item();
    return item === null ? this.listLink : this.config.routes.detail(this.config.resource.id(item));
  });

  private readonly saveFlow = new CrudSaveFlow<TResponse, TRequest>({
    save: (request) => this.update(request),
    form: () => this.entityForm(),
    fieldMap: () => this.serverFieldMap(),
    onSaved: (saved) =>
      void this.router.navigate([...this.config.routes.detail(this.config.resource.id(saved))]),
  });

  protected readonly pending = this.saveFlow.pending;
  protected readonly generalErrors = this.saveFlow.generalErrors;

  /** The concrete page exposes its form component's FormGroup here. */
  protected abstract entityForm(): FormGroup | undefined;

  /** Override when server field names differ from form control names. */
  protected serverFieldMap(): ServerFieldMap {
    return {};
  }

  protected submit(request: TRequest): void {
    this.saveFlow.submit(request);
  }

  private update(request: TRequest): Observable<TResponse> {
    const item = this.item();
    if (item === null) {
      return throwError(() => new Error('no item loaded'));
    }
    return this.config.resource.update(this.config.resource.id(item), request);
  }
}
