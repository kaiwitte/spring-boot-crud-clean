import { Component, computed, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

import { AbstractItemShellComponent } from './abstract-item-shell.component';
import { ItemField, ItemState } from './item-model';

/**
 * Generic detail-view layout: renders typed fields of a loaded item with
 * back/edit/delete actions, on top of the shared loading/not-found shell.
 */
@Component({
  selector: 'app-abstract-item',
  imports: [AbstractItemShellComponent, MatButtonModule, MatCardModule, MatIconModule, RouterLink],
  templateUrl: './abstract-item.component.html',
})
export class AbstractItemComponent<T> {
  readonly state = input.required<ItemState<T>>();
  readonly entityName = input.required<string>();
  readonly fields = input.required<readonly ItemField<T>[]>();
  /** Optional item-specific heading; falls back to the entity name. */
  readonly title = input<((item: T) => string) | null>(null);
  readonly editLink = input<readonly string[] | null>(null);
  readonly backLink = input.required<readonly string[]>();
  readonly deleteRequested = output<void>();

  protected readonly item = computed<T | null>(() => {
    const state = this.state();
    return state.kind === 'loaded' ? state.item : null;
  });

  protected heading(item: T): string {
    const titleOf = this.title();
    return titleOf === null ? this.entityName() : titleOf(item);
  }
}
