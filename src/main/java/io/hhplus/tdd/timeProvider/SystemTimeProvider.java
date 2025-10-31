package io.hhplus.tdd.timeProvider;

import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider implements TimeProvider{
    @Override
    public long currentTimeMillis(){
        return System.currentTimeMillis();
    }
}
