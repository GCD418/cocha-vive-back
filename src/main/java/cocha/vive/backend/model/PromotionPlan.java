package cocha.vive.backend.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum PromotionPlan {
    ONE_DAY(20L, Duration.ofDays(1)),
    THREE_DAYS(50L, Duration.ofDays(3)),
    ONE_WEEK(90L, Duration.ofDays(7));

    private final Long amount;

    private final Duration duration;
}
