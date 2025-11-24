package io.github.cryschan.berepository._global.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 토큰 기반 인증을 처리하는 필터 클래스입니다.
 * <p>
 * HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출하여 검증하고,
 * 유효한 경우 Spring Security의 인증 컨텍스트에 사용자 정보를 설정합니다.
 * {@link OncePerRequestFilter}를 상속하여 요청당 한 번만 실행됩니다.
 * </p>
 *
 * @author tato126
 * @since 1.0
 */
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * JWT 토큰을 검증하고 인증 정보를 설정하는 필터 로직입니다.
     * <p>
     * 다음 단계로 처리됩니다:
     * <ol>
     *   <li>Authorization 헤더에서 Bearer 토큰 추출</li>
     *   <li>토큰 검증 및 Claims 파싱</li>
     *   <li>Spring Security 인증 객체 생성</li>
     *   <li>SecurityContext에 인증 정보 저장</li>
     * </ol>
     * 토큰이 없거나 유효하지 않은 경우 다음 필터로 진행합니다.
     * </p>
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 처리 중 오류 발생 시
     * @throws IOException      I/O 오류 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7); // "Bearer " 제거 (7자)

            try {

                // 2. 토큰 검증 및 userId 추출
                Claims claims = jwtUtil.validateToken(token);
                Long userId = claims.get("userId", Long.class);

                // 3. Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        new ArrayList<>()
                );

                // 4. SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // 토큰 검증 실패 (만료, 위조... etc)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
