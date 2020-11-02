package com.authentication.jwt.users.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.authentication.jwt.users.services.UserDetailsServiceImpl;


/*
 * Tale filtro ha l’importantissimo compito di intercettare ogni richiesta che arriva sul backend per verificare validità del token e 
 * impostare come autenticata la richiesta arrivata, ricostruendo l’userdetails a partire dei claims contenuti nel token.
 */


public class AuthTokenFilter extends OncePerRequestFilter {	
	
  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserDetailsServiceImpl userDetailsService;
  
  @Value("${bezkoder.app.jwtHeader}")
  private String tokenHeader;
  
  @Value("${bezkoder.app.jwtConstant}")
  private String tokenConstant;

  private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = parseJwt(request);
      
      /*
       * Se il token viene giudicato valido viene impostato nel contesto della request l’autenticazione con relativo principal e 
       * viene invocata la chain.doFilter la quale farà scattare i  filtri di Spring Security.
       */
      
      
      if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

        String username = jwtUtils.getUserNameFromJwtToken(jwt);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception e) {
      logger.error("Cannot set user authentication: {}", e);
    }

    filterChain.doFilter(request, response);
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader(tokenHeader);

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(tokenConstant)) {
      return headerAuth.substring(7, headerAuth.length());
    }
    return null;
  }
}
