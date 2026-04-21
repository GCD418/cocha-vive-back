package cocha.vive.backend.core.aop;

import cocha.vive.backend.core.annotations.FeatureFlag;
import cocha.vive.backend.exception.FeatureDisabledException;
import cocha.vive.backend.service.FeatureToggleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.support.AopUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {
    private final FeatureToggleService featureToggleService;

    @Around("execution(* *(..)) && (@annotation(cocha.vive.backend.core.annotations.FeatureFlag) || @within(cocha.vive.backend.core.annotations.FeatureFlag))")
    public Object checkFeatureFlag(ProceedingJoinPoint pjp)
        throws Throwable {
        FeatureFlag featureFlag = resolveFeatureFlag(pjp);
        if (featureFlag == null) {
            return pjp.proceed();
        }

        String flagName = featureFlag.value().getUnleashKey();

        if(!featureToggleService.isEnabled(flagName)) {
            log.info("Blocked access by flag {} to userId = {}", flagName, resolveUserId());
            throw new FeatureDisabledException("NOT AVAILABLE FEATURE");
        }
        return pjp.proceed();
    }

    private String resolveUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(Authentication::getName)
            .orElse("anonymous");
    }

    private FeatureFlag resolveFeatureFlag(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();

        FeatureFlag methodFlag = AnnotationUtils.findAnnotation(method, FeatureFlag.class);
        if (methodFlag != null) {
            return methodFlag;
        }

        Class<?> targetClass = AopUtils.getTargetClass(pjp.getTarget());
        if (targetClass != null) {
            return AnnotationUtils.findAnnotation(targetClass, FeatureFlag.class);
        }

        return null;
    }
}
