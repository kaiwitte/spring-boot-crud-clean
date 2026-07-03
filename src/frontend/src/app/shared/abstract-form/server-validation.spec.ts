import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ApplicationErrorResponse } from '@generated';

import {
  SERVER_ERROR_KEY,
  applyServerValidationErrors,
  clearServerValidationErrors,
} from './server-validation';

const validationError = (
  overrides: Partial<ApplicationErrorResponse> = {},
): ApplicationErrorResponse => ({
  message: 'Validation failed',
  errorType: 'VALIDATION_ERROR',
  ...overrides,
});

describe('applyServerValidationErrors', () => {
  it('sets a server error on the matching control and marks it touched', () => {
    const form = new FormGroup({ name: new FormControl('') });

    const general = applyServerValidationErrors(
      form,
      validationError({ fieldErrors: { name: 'must not be blank' } }),
    );

    expect(form.controls.name.errors).toEqual({ [SERVER_ERROR_KEY]: 'must not be blank' });
    expect(form.controls.name.touched).toBe(true);
    expect(general).toEqual([]);
  });

  it('translates server field names through the field map', () => {
    const form = new FormGroup({ roomName: new FormControl('') });

    applyServerValidationErrors(
      form,
      validationError({ fieldErrors: { name: 'must not be blank' } }),
      { name: 'roomName' },
    );

    expect(form.controls.roomName.errors).toEqual({ [SERVER_ERROR_KEY]: 'must not be blank' });
  });

  it('reports field errors without a matching control as general errors', () => {
    const form = new FormGroup({ name: new FormControl('') });

    const general = applyServerValidationErrors(
      form,
      validationError({ fieldErrors: { owner: 'unknown owner' } }),
    );

    expect(general).toEqual(['owner: unknown owner']);
  });

  it('returns top-level errors as general errors', () => {
    const form = new FormGroup({ name: new FormControl('') });

    const general = applyServerValidationErrors(
      form,
      validationError({ errors: ['first problem', 'second problem'] }),
    );

    expect(general).toEqual(['first problem', 'second problem']);
  });

  it('falls back to the message when there are no field or list errors', () => {
    const form = new FormGroup({ name: new FormControl('') });

    const general = applyServerValidationErrors(form, validationError());

    expect(general).toEqual(['Validation failed']);
  });
});

describe('clearServerValidationErrors', () => {
  it('removes server errors but keeps other validation errors', () => {
    const requiredControl = new FormControl('', Validators.required);
    const form = new FormGroup({ name: requiredControl, other: new FormControl('x') });
    requiredControl.setErrors({
      required: true,
      [SERVER_ERROR_KEY]: 'must not be blank',
    });
    form.controls.other.setErrors({ [SERVER_ERROR_KEY]: 'server only' });

    clearServerValidationErrors(form);

    expect(form.controls.name.errors).toEqual({ required: true });
    expect(form.controls.other.errors).toBeNull();
  });
});
