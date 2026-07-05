import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AbstractItemShellComponent } from './abstract-item-shell.component';
import { ItemState } from './item-model';

const createShell = async (
  state: ItemState<unknown>,
): Promise<ComponentFixture<AbstractItemShellComponent>> => {
  await TestBed.configureTestingModule({
    imports: [AbstractItemShellComponent],
    providers: [provideRouter([])],
  }).compileComponents();

  const fixture = TestBed.createComponent(AbstractItemShellComponent);
  fixture.componentRef.setInput('state', state);
  fixture.componentRef.setInput('entityName', 'Room');
  fixture.componentRef.setInput('backLink', ['/rooms']);
  fixture.detectChanges();
  return fixture;
};

describe('AbstractItemShellComponent', () => {
  it('renders the not-found message for a missing item', async () => {
    const fixture = await createShell({ kind: 'not-found' });

    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('Room not found.');
  });

  it('renders the error message when loading failed, not the not-found text', async () => {
    const fixture = await createShell({ kind: 'error', message: 'Database unavailable' });

    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('Database unavailable');
    expect(text).not.toContain('not found');
  });
});
