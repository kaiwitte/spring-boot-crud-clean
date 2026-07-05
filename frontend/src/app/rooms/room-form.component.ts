import { Component, effect, inject, input, output } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { RoomRequest, RoomResponse } from '@generated';

import { AbstractFormComponent } from '../shared/abstract-form/abstract-form.component';
import { FieldErrorPipe } from '../shared/abstract-form/field-error.pipe';

/**
 * Maps the generated RoomRequest/RoomResponse types onto a reactive form;
 * submit, pending and server-error display come from the abstract form.
 */
@Component({
  selector: 'app-room-form',
  imports: [
    AbstractFormComponent,
    FieldErrorPipe,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
  ],
  template: `
    <app-abstract-form
      [form]="form"
      [pending]="pending()"
      [generalErrors]="generalErrors()"
      [submitLabel]="submitLabel()"
      [cancelLink]="cancelLink()"
      (save)="onSave()"
    >
      <div [formGroup]="form" class="flex flex-col gap-2">
        <mat-form-field appearance="outline">
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" required />
          <mat-error>{{ form.controls.name.errors | appFieldError }}</mat-error>
        </mat-form-field>
      </div>
    </app-abstract-form>
  `,
})
export class RoomFormComponent {
  readonly initial = input<RoomResponse | null>(null);
  readonly pending = input(false);
  readonly generalErrors = input<readonly string[]>([]);
  readonly submitLabel = input('Save');
  readonly cancelLink = input<readonly string[] | null>(null);
  readonly save = output<RoomRequest>();

  readonly form = inject(NonNullableFormBuilder).group({
    name: ['', Validators.required],
  });

  constructor() {
    effect(() => {
      const room = this.initial();
      if (room !== null) {
        this.form.patchValue({ name: room.name });
      }
    });
  }

  protected onSave(): void {
    this.save.emit({ name: this.form.getRawValue().name });
  }
}
