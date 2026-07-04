import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { provideApi } from '@generated';

import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        provideApi({ basePath: 'http://localhost:8080' }),
      ],
    }).compileComponents();
  });

  it('renders the toolbar with navigation to the room list', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Crud Demo');

    const roomsLink = compiled.querySelector<HTMLAnchorElement>('a[href="/rooms"]');
    expect(roomsLink?.textContent).toContain('Rooms');
  });

  it('shows rooms loaded through the generated API on the list route', async () => {
    const harness = await RouterTestingHarness.create('/rooms');
    const httpTesting = TestBed.inject(HttpTestingController);
    await harness.fixture.whenStable();

    const request = httpTesting.expectOne((req) => req.url === 'http://localhost:8080/rooms');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('10');
    request.flush({
      pagination: { total: 1, index: 0, size: 10 },
      results: [{ id: 'room-1', name: 'Blue Room' }],
    });

    await harness.fixture.whenStable();
    harness.detectChanges();

    expect(harness.routeNativeElement?.textContent).toContain('Blue Room');
  });

  it('renders the not-found page for unknown routes', async () => {
    const harness = await RouterTestingHarness.create('/does-not-exist');

    expect(harness.routeNativeElement?.textContent).toContain('Page not found');
  });
});
