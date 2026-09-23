package com.flickstream.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.flickstream.auth.AuthDtos.AuthResponse;
import static com.flickstream.auth.AuthDtos.SignInRequest;
import static com.flickstream.auth.AuthDtos.SignUpRequest;
import static com.flickstream.auth.AuthDtos.UserResponse;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Transactional
    public AuthResponse signUp(SignUpRequest request, HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String name = request.name().trim();
        String password = request.password();
        if (name.length() < 2) throw new IllegalArgumentException("Name must be at least 2 characters.");
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be no more than 72 bytes.");
        }
        if (users.existsByEmail(email)) throw new DuplicateEmailException();

        AppUser user = new AppUser(name, email, passwordEncoder.encode(password));
        try {
            users.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
        return authenticate(email, password, httpRequest, httpResponse);
    }

    public AuthResponse signIn(SignInRequest request, HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be no more than 72 bytes.");
        }
        return authenticate(request.email().trim().toLowerCase(Locale.ROOT), request.password(), httpRequest, httpResponse);
    }

    private AuthResponse authenticate(String email, String password, HttpServletRequest request,
                                      HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        if (request.getSession(false) != null) request.changeSessionId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        AccountPrincipal user = (AccountPrincipal) authentication.getPrincipal();
        return new AuthResponse(UserResponse.from(user));
    }
}
