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
 * TODO Проверьте, что ваш лимитер корректно работает.
 * <p>
 * Предполагается, что есть какое-то заданное количество потоков-обработчиков, которые выполняют задачу {@link PrintTask}:
 * <p>
 * Если не пользоваться лимитером, то каждый поток будет выводить на экран по 10 записей в секунду
 * <p>
 * Требуется ограничить общий вывод с помощью оконного лимитера,
 * чтобы количество записей на экране не превышало заданное лимитером.
 * <p>
 * Для этого реализуйте {@link RateLimiter}.accept() в классе {@link WindowRateLimiter}
 */
class RateLimiterTest {

    private static final int rate = 5;

    private static final RateLimiter limiter = new WindowRateLimiter(rate);

    //TODO Напишите проверку, что вывод в каждую секунду не превышает заданное число операций (rate).
    @Test
    void should_LimitNumberOfOutputEventsPerSecondToRateValue_When_WindowRateLimiterIsApplied() throws InterruptedException {

        //временные рамки для теста: 5 секунд
        LocalTime startTime = LocalTime.now();
        LocalTime finishTime = startTime.plus(5, ChronoUnit.SECONDS);

        System.out.println("start at " + startTime);
        System.out.println("finish at " + finishTime);
        System.out.println("--------------------");

        //Запускаем задания, каждое в своем потоке
        int nTasks = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(nTasks);

        IntStream.range(0, nTasks)
                .mapToObj(i -> UUID.randomUUID().toString())
                .map(PrintTask::create)
                .map(Executors::callable)
                .forEach(executorService::submit);

        //Данный блок кода помогает визуализировать, сколько операций было выполнено в секунду
        while (LocalTime.now().isBefore(finishTime)) {
            Thread.sleep(1000);

            int delta = LocalTime.now().getSecond() - startTime.getSecond();
            System.out.println("--------------------------  [Прошло секунд: " + delta + "] -----------------------------");
        }

        executorService.shutdownNow();
    }

    //TODO Напишите тест для случая rate = 0
    @Test
    void should_BlockAllOutputEvents_When_RateEqualsZero() {

    }

    /**
     * Задание по выводу на экран заданной строки каждые 100 млсек (10 раз в секунду)
     * TODO модифицируйте данный класс под требования задачи
     */
    static class PrintTask implements Runnable {

        private final String name;

        private PrintTask(String name) {
            this.name = name;
        }

        static PrintTask create(String name) {
            return new PrintTask("Задание: " + name);
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
                    System.out.println(name + ": номер попытки вывода: " + attempt + " время после старта [мс]: " + fromStart);
                }
            }
        }
    }
}
