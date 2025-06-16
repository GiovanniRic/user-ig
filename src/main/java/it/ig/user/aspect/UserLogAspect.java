package it.ig.user.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class UserLogAspect {

    @Before("execution(* it.ig.user.controller..*(..))")
    public void logUserLogged() {
        var auth = SecurityContextHolder.getContext().getAuthentication();


        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            var username = jwt.getClaimAsString("preferred_username");
            log.info("USER LOGGED IS {}", username);
        }
    }
}
