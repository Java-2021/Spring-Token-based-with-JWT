package springboot.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import springboot.helper.Jwtutill;
import springboot.services.CustomUserDetailsService;
@Component
public class JwtFillter extends OncePerRequestFilter {
	@Autowired
	public CustomUserDetailsService customUserDetailsService;
	@Autowired
	public Jwtutill jwtutill;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain Chain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		//get jwt
		//bearer
		//validate
		final String requestTokenHeader = request.getHeader("Authorization");

		String username = null;
		String jwtToken = null;
		// JWT Token is in the form "Bearer token". Remove Bearer word and get
		// only the Token
		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
		jwtToken = requestTokenHeader.substring(7);
		try {
		username = jwtutill.getUsernameFromToken(jwtToken);
		} catch (IllegalArgumentException e) {
		System.out.println("Unable to get JWT Token");
		} catch (ExpiredJwtException e) {
		System.out.println("JWT Token has expired");
		}
		} else {
		logger.warn("JWT Token does not begin with Bearer String");
		}

		// Once we get the token validate it.
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

		UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

		// if token is valid configure Spring Security to manually set
		// authentication
		if (jwtutill.validateToken(jwtToken, userDetails)) {

		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
		userDetails, null, userDetails.getAuthorities());
		usernamePasswordAuthenticationToken
		.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		// After setting the Authentication in the context, we specify
		// that the current user is authenticated. So it passes the
		// Spring Security Configurations successfully.
		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		}
		}
		Chain.doFilter(request, response);
		}
		
		
	}
