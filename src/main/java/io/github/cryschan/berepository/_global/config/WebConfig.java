package io.github.cryschan.berepository._global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정 클래스
 * CORS(Cross-Origin Resource Sharing) 설정 포함
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Vercel 배포 URL 허용 (배포 후 실제 URL로 변경 필요)
                .allowedOrigins(
                        "http://localhost:3000",           // 로컬 개발
                        "http://localhost:5173",           // Vite 로컬 개발
                        "https://*.vercel.app",            // Vercel 프리뷰/프로덕션
                        "https://your-domain.com"          // 커스텀 도메인 (있으면)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
