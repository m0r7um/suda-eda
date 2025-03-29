package org.food.sudaeda.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.food.sudaeda.core.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

public class FixBasicAuthFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    public FixBasicAuthFilter(UserRepository userRepository) {
       this.userRepository = userRepository;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long id = userRepository.findByUsername(username).get().getId();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.getRequest().setAttribute(USER_ID_KEY, id);
        }
        filterChain.doFilter(request, response);
    }

    private static final String USER_ID_KEY = "USER_ID";
}
