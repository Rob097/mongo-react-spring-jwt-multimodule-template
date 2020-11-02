package com.authentication.jwt.users.security.jwt;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.authentication.jwt.users.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClock;

/*
 * Tale classe costruisce il token andando a salvare nel payload del token le informazioni utili contenute nella nostra implementazione di userdetails e 
 * impostando una data di scadenza di un numero di secondi  pari al valore della properties jwtExpirationMs
 */

@Component
public class JwtUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${bezkoder.app.jwtSecret}")
	private String jwtSecret;

	@Value("${bezkoder.app.jwtExpirationMs}")
	private int jwtExpirationMs;

	static final String CLAIM_KEY_AUTHORITIES = "roles";

	private static Clock clock = DefaultClock.INSTANCE;

	public String generateJwtToken(Authentication authentication) {

		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_KEY_AUTHORITIES, userPrincipal.getAuthorities());

		return Jwts.builder().setClaims(claims).setSubject((userPrincipal.getUsername())).setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}

	private Boolean ignoreTokenExpiration(String token) {
		// here you specify tokens, for that the expiration is ignored
		return false;
	}

	private Claims getAllClaimsFromToken(String token) {
		try {
			Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
			return claims;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		} catch (Exception e) {
			logger.error("Errore Generico: {}", e.getMessage());
		}
		return null;
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		try {
			final Claims claims = getAllClaimsFromToken(token);
			return claimsResolver.apply(claims);
		} catch (Exception e) {
			throw e;
		}
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(clock.now());
	}

	public Boolean canTokenBeRefreshed(String token) {
		return (!isTokenExpired(token) || ignoreTokenExpiration(token));
	}

	private Date calculateExpirationDate(Date createdDate) {
		return new Date(createdDate.getTime() + jwtExpirationMs * 1000);
	}

	public String refreshToken(String token) {
		final Date createdDate = clock.now();
		final Date expirationDate = calculateExpirationDate(createdDate);

		final Claims claims = getAllClaimsFromToken(token);
		claims.setIssuedAt(createdDate);
		claims.setExpiration(expirationDate);

		return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
	}
}
