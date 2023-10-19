package ru.eagdort.interview.rate.limiter;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;


/**
 * TODO ���������, ��� ��� ������� ��������� ��������.
 * <p>
 * ��������������, ��� ���� �����-�� �������� ���������� �������-������������, ������� ��������� ������ {@link PrintTask}:
 * <p>
 * ���� �� ������������ ���������, �� ������ ����� ����� �������� �� ����� �� 10 ������� � �������
 * <p>
 * ��������� ���������� ����� ����� � ������� �������� ��������,
 * ����� ���������� ������� �� ������ �� ��������� �������� ���������.
 * <p>
 * ��� ����� ���������� {@link RateLimiter}.accept() � ������ {@link WindowRateLimiter}
 */
class RateLimiterTest {

    private static final int rate = 5;

    private static final RateLimiter limiter = new WindowRateLimiter(rate);

    //TODO �������� ��������, ��� ����� � ������ ������� �� ��������� �������� ����� �������� (rate).
    @Test
    void should_LimitNumberOfOutputEventsPerSecondToRateValue_When_WindowRateLimiterIsApplied() throws InterruptedException {

        //��������� ����� ��� �����: 5 ������
        LocalTime startTime = LocalTime.now();
        LocalTime finishTime = startTime.plus(5, ChronoUnit.SECONDS);

        System.out.println("start at " + startTime);
        System.out.println("finish at " + finishTime);
        System.out.println("--------------------");

        //��������� �������, ������ � ����� ������
        int nTasks = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(nTasks);

        IntStream.range(0, nTasks)
                .mapToObj(i -> UUID.randomUUID().toString())
                .map(PrintTask::create)
                .map(Executors::callable)
                .forEach(executorService::submit);

        //������ ���� ���� �������� ���������������, ������� �������� ���� ��������� � �������
        while (LocalTime.now().isBefore(finishTime)) {
            Thread.sleep(1000);

            int delta = LocalTime.now().getSecond() - startTime.getSecond();
            System.out.println("--------------------------  [������ ������: " + delta + "] -----------------------------");
        }

        executorService.shutdownNow();
    }

    //TODO �������� ���� ��� ������ rate = 0
    @Test
    void should_BlockAllOutputEvents_When_RateEqualsZero() {

    }

    /**
     * ������� �� ������ �� ����� �������� ������ ������ 100 ����� (10 ��� � �������)
     * TODO ������������� ������ ����� ��� ���������� ������
     */
    static class PrintTask implements Runnable {

        private final String name;

        private PrintTask(String name) {
            this.name = name;
        }

        static PrintTask create(String name) {
            return new PrintTask("�������: " + name);
        }

        @Override
        public void run() {
            int attempt = 0;

            LocalTime startTime = LocalTime.now();

            while (!Thread.currentThread().isInterrupted()) {
                attempt++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("Thread" + Thread.currentThread().getName() + "  is interrupted");
                    Thread.currentThread().interrupt();
                    break;
                }
                if (limiter.accept()) {
                    long fromStart = Duration.between(startTime, LocalTime.now()).toMillis();
                    System.out.println(name + ": ����� ������� ������: " + attempt + " ����� ����� ������ [��]: " + fromStart);
                }
            }
        }
    }
}
