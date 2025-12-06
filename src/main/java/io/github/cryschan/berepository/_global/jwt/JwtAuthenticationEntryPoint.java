package io.github.cryschan.berepository._global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 처리하는 핸들러
 * JWT 토큰이 없거나, 만료되었거나, 유효하지 않은 경우 401 Unauthorized 응답을 반환합니다.
 * Spring Security의 {@link AuthenticationEntryPoint}를 구현하여
 * 인증 실패 시 표준화된 에러 응답을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 인증 실패 시 호출되는 메서드
     * 401 상태 코드와 함께 JSON 형식의 에러 메시지를 반환합니다.
     * 프론트엔드에서 이 응답을 받으면 refresh token으로 access token 재발급을 시도합니다.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 필터에서 설정한 예외 정보 확인
        String exceptionType = (String) request.getAttribute("exception");
        ErrorCode errorCode = determineErrorCode(exceptionType);

        System.out.println("JwtAuthenticationEntryPoint exceptionType = " + exceptionType);
        System.out.println("authException = " + authException.getClass().getName() + " : " + authException.getMessage());

        response.setStatus(errorCode.getHttpStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", errorCode.getHttpStatusCode());
        errorResponse.put("code", errorCode.getCode());
        errorResponse.put("message", errorCode.getMessage());
        errorResponse.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * 예외 타입에 따라 적절한 ErrorCode를 반환합니다.
     */
    private ErrorCode determineErrorCode(String exceptionType) {
        if (exceptionType == null) {
            return ErrorCode.UNAUTHORIZED;
        }

        return switch (exceptionType) {
            case "EXPIRED_TOKEN" -> ErrorCode.EXPIRED_TOKEN;
            case "INVALID_TOKEN" -> ErrorCode.INVALID_TOKEN;
            default -> ErrorCode.UNAUTHORIZED;
        };
    }
}