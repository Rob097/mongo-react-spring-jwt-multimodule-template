package com.authentication.jwt.users.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.authentication.jwt.users.models.ERole;
import com.authentication.jwt.users.models.Role;
import com.authentication.jwt.users.models.User;
import com.authentication.jwt.users.payload.request.LoginRequest;
import com.authentication.jwt.users.payload.request.SignupRequest;
import com.authentication.jwt.users.payload.response.JwtResponse;
import com.authentication.jwt.users.payload.response.MessageResponse;
import com.authentication.jwt.users.repository.RoleRepository;
import com.authentication.jwt.users.repository.UserRepository;
import com.authentication.jwt.users.security.jwt.JwtUtils;
import com.authentication.jwt.users.services.UserDetailsImpl;

/**
 * @author Roberto97 Class used to perform action on the paths that are relative
 *         to an authentication action.
 */
@CrossOrigin(origins = "*", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private JwtUtils jwtUtils;

	@Value("${jwtHeader}")
	private String tokenHeader;

	@Value("${jwtExpirationMs}")
	private int jwtExpirationMs;
	@Value("${jwtExpirationMsRememberMe}")
	private long jwtExpirationMs_rememberMe;
	
	private final String tokenCookieName = "token";
	private final String rememberCookieName = "rememberMe";
	private final String domainNameCookies = "localhost";
	private final String pathCookies = "/";
	
	private Cookie token_cookie = null;
	private Cookie remember_cookie = null;

	/**
	 * Used when a user try to login. It check if the params are ok and if they are,
	 * it creates and response with a JWT token.
	 * 
	 * @param loginRequest : Encapsulation of main parameters used to login
	 *                     (username and password).
	 * @param response
	 * @return JwtResponse with the token and the roles.
	 */
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
			HttpServletResponse response) {
		Authentication authentication;
		try {
		 authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		}catch(Exception e) {
			System.out.println(e);
			return ResponseEntity.badRequest().body(new MessageResponse("Attenzione! Le credenziali non sono corrette"));
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String token = jwtUtils.generateJwtToken(authentication, loginRequest.isRememberMe());

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		System.out.println(loginRequest.isRememberMe());
		
		
		
		if (setCookie(loginRequest.isRememberMe(), token, response)) {
			if(token_cookie != null)
				response.addCookie(token_cookie);
			if(remember_cookie != null)
				response.addCookie(remember_cookie);
				return ResponseEntity.ok(new JwtResponse(token, userDetails.getId(), userDetails.getUsername(),
						userDetails.getEmail(), roles));
		}

		return ResponseEntity.badRequest().body(new MessageResponse("Errore: Qualcosa è andato storto!"));		
	}

	/**
	 * Method used when a token is going to expire and a user chose to refresh it.
	 * 
	 * @param request
	 * @param response
	 * @return Bad request if something went wrong or an JwtResponse with the new
	 *         token
	 */
	@RequestMapping(value = "/refresh-token", method = RequestMethod.GET)
	public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request,
			HttpServletResponse response) {

		String authToken = request.getHeader(tokenHeader);
		boolean rememberMe = false;

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			check: for (Cookie cookie : cookies) {
				if (cookie.getName().equals("rememberMe") && cookie.getValue().equals("true")) {
					rememberMe = true;
					break check;
				}
			}
		}

		final String token = authToken.substring(7);

		if (jwtUtils.canTokenBeRefreshed(token)) {
			String refreshedToken = jwtUtils.refreshToken(token, rememberMe);
			response.setHeader(tokenHeader, refreshedToken);

			response.setHeader("exp", jwtUtils.getExpirationDateFromToken(refreshedToken).toString());

			if (setCookie(rememberMe, refreshedToken, response))
				return ResponseEntity.ok(new JwtResponse(refreshedToken));

		}

		return ResponseEntity.badRequest().body(null);
	}

	/**
	 * Method used when a user want to sign up to the application. It also check the
	 * roles.
	 * 
	 * @param signUpRequest
	 * @return badRequest if something went wrong or a ok response instead.
	 */
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(/*@Valid*/ @RequestBody SignupRequest signUpRequest) {
		System.out.println("SignupRequest: " + signUpRequest);
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRoles();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "mod":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
				case "user":
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	public boolean setCookie(boolean rememberMe, String token, HttpServletResponse response) {

		try {
			remember_cookie = new Cookie(rememberCookieName, "" + rememberMe);
			remember_cookie.setSecure(false); // Set this to true if you're working through https
			remember_cookie.setHttpOnly(false);
			remember_cookie.setDomain(domainNameCookies);
			remember_cookie.setPath(pathCookies); // global cookie accessible every where

			token_cookie = new Cookie(tokenCookieName, token);
			token_cookie.setSecure(false); // Set this to true if you're working through https
			token_cookie.setHttpOnly(false);
			token_cookie.setDomain(domainNameCookies);
			token_cookie.setPath(pathCookies); // global cookie accessible every where

			if (!rememberMe) {
				token_cookie.setMaxAge(jwtExpirationMs / 1000);
				remember_cookie.setMaxAge(jwtExpirationMs / 1000);
			} else {
				token_cookie.setMaxAge((int) (jwtExpirationMs_rememberMe / 1000));
				remember_cookie.setMaxAge((int) (jwtExpirationMs_rememberMe / 1000));
			}

			return true;
		} catch (Exception e) {
			System.out.println(e);
		}

		return false;
	}

}
