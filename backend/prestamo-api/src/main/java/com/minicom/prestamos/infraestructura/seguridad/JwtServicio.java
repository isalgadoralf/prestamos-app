package com.minicom.prestamos.infraestructura.seguridad;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtServicio {

    private final SecretKey claveFirma;
    private final long expiracionMilis;

    public JwtServicio(@Value("${app.jwt.secreto}") String secreto,
            @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos) {
        this.claveFirma = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        this.expiracionMilis = expiracionMinutos * 60_000;
    }

    public String generarToken(UsuarioAutenticado usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMilis);

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claims(Map.of(
                        "usuarioId", usuario.getUsuarioId(),
                        "rol", usuario.getUsuario().getRol().name()))
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(claveFirma)
                .compact();
    }

    public String extraerCorreo(String token) {
        return obtenerClaims(token).getSubject();
    }

    public boolean esTokenValido(String token, String correoEsperado) {
        Claims claims = obtenerClaims(token);
        return claims.getSubject().equals(correoEsperado) && claims.getExpiration().after(new Date());
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(claveFirma)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
