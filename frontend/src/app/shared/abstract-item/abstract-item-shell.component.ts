import { Component, computed, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';

import { ItemState } from './item-model';

/**
 * Wraps route-loaded content with the shared loading, not-found and error
 * handling; projects its content only once the item is loaded.
 */
@Component({
  selector: 'app-abstract-item-shell',
  imports: [MatButtonModule, MatCardModule, MatProgressSpinnerModule, RouterLink],
  templateUrl: './abstract-item-shell.component.html',
})
export class AbstractItemShellComponent {
  readonly state = input.required<ItemState<unknown>>();
  readonly entityName = input.required<string>();
  readonly backLink = input.required<readonly string[]>();

  /** Extracted here because @switch does not narrow the state union in the template. */
  protected readonly errorMessage = computed<string | null>(() => {
    const state = this.state();
    return state.kind === 'error' ? state.message : null;
  });
}
