package cocha.vive.backend.core.aop;

import cocha.vive.backend.core.annotations.FeatureFlag;
import cocha.vive.backend.exception.FeatureDisabledException;
import cocha.vive.backend.service.FeatureToggleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {
    private final FeatureToggleService featureToggleService;

    @Around("@annotation(featureFlag)")
    public Object checkFeatureFlag(ProceedingJoinPoint pjp, FeatureFlag featureFlag)
        throws Throwable {
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
}
