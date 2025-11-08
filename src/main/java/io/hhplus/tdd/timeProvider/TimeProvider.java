package io.hhplus.tdd.timeProvider;

import org.springframework.stereotype.Component;

@Component
public interface TimeProvider {
    long currentTimeMillis();
}
