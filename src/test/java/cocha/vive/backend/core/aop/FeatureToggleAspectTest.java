package cocha.vive.backend.core.aop;

import cocha.vive.backend.core.annotations.FeatureFlag;
import cocha.vive.backend.core.enums.AppFeature;
import cocha.vive.backend.exception.FeatureDisabledException;
import cocha.vive.backend.service.FeatureToggleService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeatureToggleAspect tests")
class FeatureToggleAspectTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private FeatureToggleAspect featureToggleAspect;

    @Test
    @DisplayName("method-level annotation should be evaluated")
    void methodLevelAnnotation_shouldBeEvaluated() throws Throwable {
        ProceedingJoinPoint pjp = joinPointFor(AnnotatedMethods.class, "methodAnnotated");
        when(featureToggleService.isEnabled("view-upcoming-events")).thenReturn(true);
        when(pjp.proceed()).thenReturn("ok");

        Object result = featureToggleAspect.checkFeatureFlag(pjp);

        assertEquals("ok", result);
        verify(featureToggleService).isEnabled("view-upcoming-events");
        verify(pjp).proceed();
    }

    @Test
    @DisplayName("class-level annotation should be evaluated when method has none")
    void classLevelAnnotation_shouldBeEvaluatedWhenMethodHasNone() throws Throwable {
        ProceedingJoinPoint pjp = joinPointFor(ClassAnnotated.class, "plainMethod");
        when(featureToggleService.isEnabled("view-featured-events")).thenReturn(true);
        when(pjp.proceed()).thenReturn("ok");

        Object result = featureToggleAspect.checkFeatureFlag(pjp);

        assertEquals("ok", result);
        verify(featureToggleService).isEnabled("view-featured-events");
    }

    @Test
    @DisplayName("method-level annotation should override class-level annotation")
    void methodLevelAnnotation_shouldOverrideClassLevel() throws Throwable {
        ProceedingJoinPoint pjp = joinPointFor(ClassAndMethodAnnotated.class, "methodAnnotated");
        when(featureToggleService.isEnabled("manage-publisher-requests")).thenReturn(true);
        when(pjp.proceed()).thenReturn("ok");

        Object result = featureToggleAspect.checkFeatureFlag(pjp);

        assertEquals("ok", result);
        verify(featureToggleService).isEnabled("manage-publisher-requests");
        verify(featureToggleService, never()).isEnabled("view-featured-events");
    }

    @Test
    @DisplayName("disabled flag should throw FeatureDisabledException")
    void disabledFlag_shouldThrowFeatureDisabledException() throws Throwable {
        ProceedingJoinPoint pjp = joinPointFor(AnnotatedMethods.class, "methodAnnotated");
        when(featureToggleService.isEnabled("view-upcoming-events")).thenReturn(false);

        assertThrows(FeatureDisabledException.class, () -> featureToggleAspect.checkFeatureFlag(pjp));
        verify(pjp, never()).proceed();
    }

    private ProceedingJoinPoint joinPointFor(Class<?> targetClass, String methodName) throws Exception {
        Method method = targetClass.getDeclaredMethod(methodName);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);

        when(pjp.getSignature()).thenReturn((Signature) methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        lenient().when(pjp.getTarget()).thenReturn(targetClass.getDeclaredConstructor().newInstance());

        return pjp;
    }

    static class AnnotatedMethods {
        @FeatureFlag(AppFeature.VIEW_UPCOMING_EVENTS)
        public void methodAnnotated() {
        }
    }

    @FeatureFlag(AppFeature.VIEW_FEATURED_EVENTS)
    static class ClassAnnotated {
        public void plainMethod() {
        }
    }

    @FeatureFlag(AppFeature.VIEW_FEATURED_EVENTS)
    static class ClassAndMethodAnnotated {
        @FeatureFlag(AppFeature.MANAGE_PUBLISHER_REQUESTS)
        public void methodAnnotated() {
        }
    }
}
