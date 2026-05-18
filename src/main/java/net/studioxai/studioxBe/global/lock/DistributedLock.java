package net.studioxai.studioxBe.global.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락 키 (SpEL 표현식 지원, e.g. "'ai:generate:user:' + #userId")
     */
    String key();

    /**
     * 락 획득 대기 시간 (기본 5초)
     */
    long waitTime() default 5;

    /**
     * 락 점유 시간 (기본 120초 — AI 생성 최대 소요 시간 고려)
     */
    long leaseTime() default 120;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
