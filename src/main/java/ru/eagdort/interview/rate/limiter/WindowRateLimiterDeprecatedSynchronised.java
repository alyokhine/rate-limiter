package ru.eagdort.interview.rate.limiter;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class WindowRateLimiterDeprecatedSynchronised implements RateLimiter {

    private final int rate; // Желаемое количество операций в секунду
    private final AtomicInteger operationCount = new AtomicInteger(0); // Количество операций за текущую секунду
    private AtomicLong lastUpdateTime; // Время последнего обновления

    public WindowRateLimiterDeprecatedSynchronised(int rate) {
        this.rate = rate;
    }

    @Override
    public synchronized boolean accept() {
        if (rate == 0) {
            return false; // Ничего не пропускаем
        }
        if (lastUpdateTime == null) {
            lastUpdateTime = new AtomicLong(Instant.now().toEpochMilli());
        }

        long lastUpdate = lastUpdateTime.get();
        long currentTime = Instant.now().toEpochMilli();

        if (currentTime - lastUpdate >= 1000) {
            // Секунда прошла, сбрасываем счетчик
            operationCount.set(0);
            lastUpdateTime.set(currentTime);
        }

        // Проверяем количество операций
        if (operationCount.get() < rate) {
            operationCount.incrementAndGet();
            return true;
        }

        return false;
    }
}
