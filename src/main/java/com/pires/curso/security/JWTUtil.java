package com.pires.curso.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JWTUtil {
	
	@Value("${jwt.secret}")
	private String secret;
	
	@Value("${jwt.expiration}")
	private Long expiration;
	
	public String generateToken(String username) {
		return Jwts.builder()
			.setSubject(username)
			.setExpiration(new Date(System.currentTimeMillis() + expiration))
			.signWith(SignatureAlgorithm.HS512, secret.getBytes())
			.compact();
	}

	public boolean tokenValido(String token) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getUsername(String token) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
