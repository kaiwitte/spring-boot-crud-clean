import { HttpErrorResponse } from '@angular/common/http';

import { ApplicationErrorResponse } from '@generated';

const ERROR_TYPES: readonly string[] = Object.values(ApplicationErrorResponse.ErrorTypeEnum);

/**
 * Extracts the structured ApplicationErrorResponse payload from a failed
 * HTTP call, or null when the error is not in the documented shape.
 */
export function toApplicationError(error: unknown): ApplicationErrorResponse | null {
  if (!(error instanceof HttpErrorResponse)) {
    return null;
  }
  const body: unknown = error.error;
  if (typeof body !== 'object' || body === null) {
    return null;
  }
  const candidate = body as Partial<ApplicationErrorResponse>;
  if (typeof candidate.message !== 'string' || typeof candidate.errorType !== 'string') {
    return null;
  }
  if (!ERROR_TYPES.includes(candidate.errorType)) {
    return null;
  }
  return candidate as ApplicationErrorResponse;
}

/** Human-readable fallback message for any error object. */
export function errorMessage(error: unknown, fallback = 'An unexpected error occurred.'): string {
  const applicationError = toApplicationError(error);
  return applicationError?.message ?? fallback;
}
