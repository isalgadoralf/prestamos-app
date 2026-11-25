import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { App } from './app';
import { NotificacionServicio } from './core/servicios/notificacion.servicio';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('se crea la aplicación', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renderiza el router-outlet y las notificaciones globales', async () => {
    const fixture = TestBed.createComponent(App);
    TestBed.inject(NotificacionServicio).exito('Hola');
    fixture.detectChanges();
    await fixture.whenStable();

    const html = fixture.nativeElement as HTMLElement;
    expect(html.querySelector('router-outlet')).not.toBeNull();
    expect(html.querySelector('.toast')?.textContent).toContain('Hola');
  });
});
