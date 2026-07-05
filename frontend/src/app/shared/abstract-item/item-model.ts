export type ItemFieldValue = string | number | boolean | null | undefined;

/** Typed field configuration mapping a loaded item to the detail layout. */
export interface ItemField<T> {
  readonly label: string;
  readonly value: (item: T) => ItemFieldValue;
}

/** Lifecycle of a route-loaded item, driving loading/not-found/error handling. */
export type ItemState<T> =
  | { readonly kind: 'loading' }
  | { readonly kind: 'not-found' }
  | { readonly kind: 'error'; readonly message: string }
  | { readonly kind: 'loaded'; readonly item: T };
