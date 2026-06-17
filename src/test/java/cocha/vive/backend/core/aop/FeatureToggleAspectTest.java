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
    @DisplayName("disabled flag should throw FeatureDisabledException")
    void disabledFlag_shouldThrowFeatureDisabledException() throws Throwable {
        ProceedingJoinPoint pjp = joinPointFor(AnnotatedMethods.class, "methodAnnotated");
        when(featureToggleService.isEnabled("send-new-event-notification-email")).thenReturn(false);

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
        @FeatureFlag(AppFeature.SEND_NEW_EVENT_NOTIFICATION_EMAIL)
        public void methodAnnotated() {
        }
    }
}
