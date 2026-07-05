import { Pipe, PipeTransform } from '@angular/core';
import { ValidationErrors } from '@angular/forms';

import { SERVER_ERROR_KEY } from './server-validation';

/**
 * Translates the first relevant validation error of a control into a
 * message, preferring server-reported messages over client validators.
 * Usage: {{ form.controls.name.errors | appFieldError }}
 */
@Pipe({ name: 'appFieldError' })
export class FieldErrorPipe implements PipeTransform {
  transform(errors: ValidationErrors | null | undefined): string {
    if (errors === null || errors === undefined) {
      return '';
    }
    if (typeof errors[SERVER_ERROR_KEY] === 'string') {
      return errors[SERVER_ERROR_KEY];
    }
    if (errors['required'] !== undefined) {
      return 'This field is required.';
    }
    if (errors['maxlength'] !== undefined) {
      return `Maximum length is ${errors['maxlength'].requiredLength}.`;
    }
    if (errors['minlength'] !== undefined) {
      return `Minimum length is ${errors['minlength'].requiredLength}.`;
    }
    return 'Invalid value.';
  }
}
