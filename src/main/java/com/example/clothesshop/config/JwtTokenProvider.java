package com.example.clothesshop.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

	@Value("${app.jwt.secret:changeit-to-a-strong-secret-with-32+chars}")
	private String secretKey;

	@Value("${app.jwt.expiration-in-ms:3600000}")
	private long validityInMilliseconds;

	private Key key;

	@PostConstruct
	public void init() {
		// create a signing key from secret
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
	}

	public String createToken(String username, List<String> roles) {
		Claims claims = Jwts.claims().setSubject(username);
		claims.put("roles", roles);
		Date now = new Date();
		Date validity = new Date(now.getTime() + validityInMilliseconds);

		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(validity)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public Authentication getAuthentication(String token) {
		String username = getUsername(token);
		List<String> roles = getRoles(token);
		List<SimpleGrantedAuthority> authorities = roles.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
		return new UsernamePasswordAuthenticationToken(username, token, authorities);
	}

	public String getUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build()
				.parseClaimsJws(token).getBody().getSubject();
	}

	public List<String> getRoles(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
				.parseClaimsJws(token).getBody();
		Object roles = claims.get("roles");
		if (roles instanceof List) {
			return ((List<?>) roles).stream().map(Object::toString).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

}
