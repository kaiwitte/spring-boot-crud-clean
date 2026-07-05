import { HttpErrorResponse } from '@angular/common/http';

import { errorMessage, toApplicationError } from './api-error';

const payload = {
  message: 'Validation failed',
  errorType: 'VALIDATION_ERROR',
  fieldErrors: { name: 'must not be blank' },
};

describe('toApplicationError', () => {
  it('extracts the structured payload from an HttpErrorResponse', () => {
    const httpError = new HttpErrorResponse({ status: 400, error: payload });

    expect(toApplicationError(httpError)).toEqual(payload);
  });

  it('returns null for a non-JSON error body', () => {
    const httpError = new HttpErrorResponse({ status: 500, error: 'boom' });

    expect(toApplicationError(httpError)).toBeNull();
  });

  it('returns null for a JSON body that is not an ApplicationErrorResponse', () => {
    const httpError = new HttpErrorResponse({ status: 500, error: { hello: 'world' } });

    expect(toApplicationError(httpError)).toBeNull();
  });

  it('returns null for non-HTTP errors', () => {
    expect(toApplicationError(new Error('boom'))).toBeNull();
  });
});

describe('errorMessage', () => {
  it('prefers the structured message of an application error', () => {
    const httpError = new HttpErrorResponse({ status: 400, error: payload });

    expect(errorMessage(httpError)).toBe('Validation failed');
  });

  it('falls back to the provided default for unknown errors', () => {
    expect(errorMessage('boom', 'Something broke.')).toBe('Something broke.');
  });
});
