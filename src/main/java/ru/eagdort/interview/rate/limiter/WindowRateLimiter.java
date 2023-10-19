package ru.eagdort.interview.rate.limiter;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WindowRateLimiter implements RateLimiter {

    private final int rate;
    private final AtomicLong acceptedCount;
    private final Lock lock;
    private final AtomicLong lastSecondStart;

    public WindowRateLimiter(int rate) {
        this.rate = rate;
        this.acceptedCount = new AtomicLong(0);
        this.lock = new ReentrantLock();
        this.lastSecondStart = new AtomicLong(0L);
    }

    @Override
    public boolean accept() {
        if (rate == 0) {
            return false;
        }
        if (lastSecondStart.get() == 0) {
            lastSecondStart.compareAndSet(0L, Instant.now().toEpochMilli() / 1000);
        }

        long currentTime = Instant.now().toEpochMilli() / 1000;
        if (currentTime > lastSecondStart.get()) {
            lock.lock();
            try {
                if (currentTime > lastSecondStart.get()) {
                    lastSecondStart.set(currentTime);
                    acceptedCount.set(0);
                }
            } finally {
                lock.unlock();
            }
        }


        return acceptedCount.incrementAndGet() <= rate;
    }
}
