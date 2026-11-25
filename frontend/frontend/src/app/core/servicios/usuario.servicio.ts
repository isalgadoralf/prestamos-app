import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_URL } from '../config/api.config';
import { Usuario } from '../modelos/usuario.modelo';

@Injectable({ providedIn: 'root' })
export class UsuarioServicio {
  private readonly http = inject(HttpClient);

  listarTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${API_URL}/usuarios`);
  }
}
