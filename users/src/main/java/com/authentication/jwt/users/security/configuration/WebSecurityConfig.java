package com.authentication.jwt.users.security.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.authentication.jwt.users.security.jwt.AuthEntryPointJwt;
import com.authentication.jwt.users.security.jwt.AuthTokenFilter;
import com.authentication.jwt.users.services.UserDetailsServiceImpl;

/*
 * E’ la classe fondamentale per configurare Spring Security.
 */


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
		// securedEnabled = true,
		// jsr250Enabled = true,
		prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final String[] USER_MATCHER = { "/api/clienti/cerca/**"};
	private static final String[] ADMIN_MATCHER = { "/api/clienti/inserisci/**", "/api/clienti/elimina/**" };
	private static final String[] MOD_MATCHER = {};
	
	@Autowired
	UserDetailsServiceImpl userDetailsService;

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;

	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}

	@Override
	public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	//Per criptare la password viene utilizzato bCrypt
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
			.cors().and()
			.csrf().disable()
			.formLogin().disable()
			.httpBasic().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			.authorizeRequests()		
			//.anyRequest().permitAll();
			.antMatchers("/*.css", "/*.png", "/*.js", "/*.json", "/*.ico").permitAll()
			.antMatchers("/static/**").permitAll()//Molto importante per il primo caricamento
			.antMatchers(HttpMethod.OPTIONS).permitAll()
			.antMatchers("/api/auth/**").permitAll()
			.antMatchers("/api/test/**").permitAll()
			.antMatchers(USER_MATCHER).hasAnyRole("USER")
			.antMatchers(ADMIN_MATCHER).hasAnyRole("ADMIN")
			.antMatchers(MOD_MATCHER).hasAnyRole("MODERATOR")
			.anyRequest().authenticated();
			
		
		//http.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues());

		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
		
		// disable page caching
		http.headers().cacheControl();
	}
	
	/*@Override
	public void configure(WebSecurity webSecurity) throws Exception {
		webSecurity.ignoring().antMatchers(HttpMethod.POST, "/auth")
				.antMatchers(HttpMethod.OPTIONS, "/**")
				.and().ignoring()
				.antMatchers(HttpMethod.GET);
	}*/
}
