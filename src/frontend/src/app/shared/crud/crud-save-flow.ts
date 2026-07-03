import { signal } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Observable, finalize } from 'rxjs';

import { errorMessage, toApplicationError } from '../api/api-error';
import {
  ServerFieldMap,
  applyServerValidationErrors,
  clearServerValidationErrors,
} from '../abstract-form/server-validation';

export interface CrudSaveFlowOptions<TResponse, TRequest> {
  save(request: TRequest): Observable<TResponse>;
  /** The concrete form, once it exists; undefined routes all errors to the banner. */
  form(): FormGroup | undefined;
  fieldMap?(): ServerFieldMap;
  onSaved(saved: TResponse): void;
}

/**
 * Shared submit workflow for create and edit flows: pending state,
 * server-side validation mapping onto the form, and success navigation.
 */
export class CrudSaveFlow<TResponse, TRequest> {
  private readonly pendingState = signal(false);
  private readonly generalErrorsState = signal<readonly string[]>([]);

  readonly pending = this.pendingState.asReadonly();
  readonly generalErrors = this.generalErrorsState.asReadonly();

  constructor(private readonly options: CrudSaveFlowOptions<TResponse, TRequest>) {}

  submit(request: TRequest): void {
    if (this.pendingState()) {
      return;
    }
    const form = this.options.form();
    if (form !== undefined) {
      clearServerValidationErrors(form);
    }
    this.generalErrorsState.set([]);
    this.pendingState.set(true);

    this.options
      .save(request)
      .pipe(finalize(() => this.pendingState.set(false)))
      .subscribe({
        next: (saved) => this.options.onSaved(saved),
        error: (error: unknown) => this.handleError(error),
      });
  }

  private handleError(error: unknown): void {
    const applicationError = toApplicationError(error);
    const form = this.options.form();
    if (applicationError === null || form === undefined) {
      this.generalErrorsState.set([errorMessage(error, 'Saving failed. Please try again.')]);
      return;
    }
    this.generalErrorsState.set(
      applyServerValidationErrors(form, applicationError, this.options.fieldMap?.() ?? {}),
    );
  }
}
