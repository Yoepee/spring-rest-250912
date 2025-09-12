package com.back.global.aspect;

import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect // AOP를 사용해 공통 로직을 주입하는 클래스임을 명시
@Component // 스프링 빈으로 등록
public class ResponseAspect {
    // 스프링이 제공하는 HttpServletResponse 객체 (응답 조작에 사용)
    private final HttpServletResponse response;

    public ResponseAspect(HttpServletResponse response) {
        this.response = response;
    }

    @Around("""
                execution(public com.back.global.rsData.RsData *(..)) &&
                (
                    within(@org.springframework.stereotype.Controller *) ||
                    within(@org.springframework.web.bind.annotation.RestController *)
                ) &&
                (
                    @annotation(org.springframework.web.bind.annotation.GetMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PostMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PutMapping) ||
                    @annotation(org.springframework.web.bind.annotation.DeleteMapping) ||
                    @annotation(org.springframework.web.bind.annotation.RequestMapping)
                )
            """)
    public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        // 원래 대상 메서드를 실행
        Object proceed = joinPoint.proceed();

        // 반환 객체가 RsData타입인 경우
        if (proceed instanceof RsData) {
            RsData<?> rsData = (RsData<?>) proceed;

            // RsData가 가진 statusCode() 값을 HTTP 응답 상태 코드로 변경
            response.setStatus(rsData.statusCode());
        }

        return proceed;
    }
}
