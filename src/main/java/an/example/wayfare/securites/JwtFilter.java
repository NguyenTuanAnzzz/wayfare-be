package an.example.wayfare.securites;

import an.example.wayfare.services.CustomUserDetailsService;
import an.example.wayfare.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
            throws ServletException {

        String path = request.getServletPath();

        return path.startsWith("/oauth2/")
                || path.startsWith("/login/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy Authorization header
        String authHeader = request.getHeader("Authorization");

        // Không có token → cho request đi tiếp
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 2. Lấy JWT
        String token = authHeader.substring(7);

        try {

            // 3. Lấy email từ JWT
            String email = jwtService.extractUsername(token);

            // 4. Kiểm tra đây có phải ACCESS TOKEN không
            String tokenType = jwtService.extractTokenType(token);

            if (!"ACCESS".equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 5. Chưa có Authentication trong SecurityContext
            if (email != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                // 6. Lấy user từ database
                UserPrincipal userPrincipal =
                        (UserPrincipal)
                                userDetailsService
                                        .loadUserByUsername(email);

                // 7. Kiểm tra JWT có hợp lệ không
                if (jwtService.isTokenValid(
                        token,
                        userPrincipal
                )) {

                    // 8. Tạo Authentication
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal,
                                    null,
                                    userPrincipal.getAuthorities()
                            );

                    // 9. Gắn thông tin request
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // 10. Đưa user vào SecurityContext
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception e) {
            // JWT lỗi / hết hạn / không hợp lệ
            // Không set Authentication
        }

        // 11. Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}