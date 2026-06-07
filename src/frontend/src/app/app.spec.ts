import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideApi } from '../src/generated';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideHttpClientTesting(),
        provideApi({ basePath: 'http://localhost:8080' }),
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  //UNREVIEWED_AI_CODE
  it('should create a room when the button is clicked', () => {
    const fixture = TestBed.createComponent(App);
    const httpTesting = TestBed.inject(HttpTestingController);

    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const button = compiled.querySelector('button');

    button?.click();

    const request = httpTesting.expectOne('http://localhost:8080/rooms');
    expect(request.request.method).toBe('POST');
    request.flush({ id: 'room-1', name: 'Room test' });

    fixture.detectChanges();

    expect(compiled.textContent).toContain('Created room room-1');
    expect(compiled.textContent).toContain('name: Room test');
  });
});
