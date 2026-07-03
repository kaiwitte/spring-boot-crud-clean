import { FormGroup } from '@angular/forms';

import { ApplicationErrorResponse } from '@generated';

/** Error key used on form controls for server-reported field errors. */
export const SERVER_ERROR_KEY = 'server';

/**
 * Optional mapping from server field names (as they appear in
 * ApplicationErrorResponse.fieldErrors) to form control names.
 */
export type ServerFieldMap = Readonly<Record<string, string>>;

/**
 * Maps an ApplicationErrorResponse onto a reactive form: field errors land
 * on their controls under SERVER_ERROR_KEY, everything unmappable is
 * returned as general error messages for the form-level banner.
 */
export function applyServerValidationErrors(
  form: FormGroup,
  error: ApplicationErrorResponse,
  fieldMap: ServerFieldMap = {},
): string[] {
  const general: string[] = [];

  for (const [field, message] of Object.entries(error.fieldErrors ?? {})) {
    const controlName = fieldMap[field] ?? field;
    const control = form.get(controlName);
    if (control === null) {
      general.push(`${field}: ${message}`);
      continue;
    }
    control.setErrors({ ...control.errors, [SERVER_ERROR_KEY]: message });
    control.markAsTouched();
  }

  general.push(...(error.errors ?? []));

  if (general.length === 0 && Object.keys(error.fieldErrors ?? {}).length === 0) {
    general.push(error.message);
  }

  return general;
}

/** Removes SERVER_ERROR_KEY errors from all controls, keeping other errors. */
export function clearServerValidationErrors(form: FormGroup): void {
  for (const control of Object.values(form.controls)) {
    if (control.errors === null || !(SERVER_ERROR_KEY in control.errors)) {
      continue;
    }
    const { [SERVER_ERROR_KEY]: ignored, ...remaining } = control.errors;
    control.setErrors(Object.keys(remaining).length === 0 ? null : remaining);
  }
}
