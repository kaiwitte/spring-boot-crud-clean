import { Directive, inject } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

import { ServerFieldMap } from '../abstract-form/server-validation';
import { injectCrudResource } from '../crud/crud-resource';
import { CrudSaveFlow } from '../crud/crud-save-flow';

/**
 * Generic create flow: submits the concrete form through the generated
 * create call, maps server validation errors back onto the form and
 * redirects to the new item's detail view on success.
 */
@Directive()
export abstract class AbstractCreatorComponent<TResponse, TRequest> {
  protected readonly config = injectCrudResource<TResponse, TRequest>();
  private readonly router = inject(Router);

  protected readonly listLink: string[] = [...this.config.routes.list()];

  private readonly saveFlow = new CrudSaveFlow<TResponse, TRequest>({
    save: (request) => this.config.resource.create(request),
    form: () => this.entityForm(),
    fieldMap: () => this.serverFieldMap(),
    onSaved: (created) =>
      void this.router.navigate([...this.config.routes.detail(this.config.resource.id(created))]),
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
}
