package com.pires.curso.security;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pires.curso.dto.CredenciaisDTO;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	private JWTUtil jWTUtil;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTUtil jWTUtil) {
		setAuthenticationFailureHandler(new JWTAuthenticationFailureHandler());
		this.authenticationManager = authenticationManager;
		this.jWTUtil = jWTUtil;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {

		try {
			// Converte os dados da requisição para os campos do DTO
			CredenciaisDTO creds = new ObjectMapper().readValue(req.getInputStream(), CredenciaisDTO.class);
			// Cria um objeto para cumprir as regras do spring security
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(creds.getEmail(),
					creds.getSenha(), new ArrayList<>());
			// Verifica se as credencias são validas com base no UserDetailsServiceImpl
			Authentication auth = authenticationManager.authenticate(authToken);
			// Devolve para o Spring security
			return auth;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, ServletException {

		// auth é gerado pelo método acima
		String username = ((UserSS) auth.getPrincipal()).getUsername();
		String token = jWTUtil.generateToken(username);
		res.addHeader("Authorization", "Bearer " + token);
		 res.addHeader("access-control-expose-headers", "Authorization");
	}
	
	

}
