import { Component, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';

/**
 * Generic form shell: hosts the <form> element, renders general
 * (non-field) server errors, and owns submit/pending/cancel behavior.
 * Concrete fields are projected; wrap them in an element carrying
 * [formGroup] so formControlName resolves in the projected content.
 */
@Component({
  selector: 'app-abstract-form',
  imports: [MatButtonModule, MatProgressSpinnerModule, ReactiveFormsModule, RouterLink],
  templateUrl: './abstract-form.component.html',
})
export class AbstractFormComponent {
  readonly form = input.required<FormGroup>();
  readonly pending = input(false);
  readonly generalErrors = input<readonly string[]>([]);
  readonly submitLabel = input('Save');
  readonly cancelLink = input<readonly string[] | null>(null);
  readonly save = output<void>();

  protected onSubmit(): void {
    if (this.pending()) {
      return;
    }
    const form = this.form();
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    this.save.emit();
  }
}
