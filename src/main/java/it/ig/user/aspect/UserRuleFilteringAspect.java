package it.ig.user.aspect;

import it.ig.user.domain.model.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class UserRuleFilteringAspect {


    @Around("@annotation(it.ig.user.controller.annotation.UserRuleFiltering)")
    public Object afterReturning(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();


        if (result instanceof ResponseEntity<?> response) {
            Object body = response.getBody();
            if (body instanceof UserDTO userDTO) {

                var role = SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority).toList().stream().findFirst();

                UserDTO newDto = switch (role.get()) {
                    case "ROLE_OPERATOR" -> new UserDTO(userDTO.id(), userDTO.name(), userDTO.surname(),
                            userDTO.email(), userDTO.username(), null,
                            userDTO.roles());
                    case "ROLE_USER" ->  new UserDTO(userDTO.id(), userDTO.name(), userDTO.surname(),
                            userDTO.email(), userDTO.username(), null, null);
                    default -> userDTO;
                };
                return ResponseEntity
                        .status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .body(newDto);

            }

        }

        return result;
    }
}


