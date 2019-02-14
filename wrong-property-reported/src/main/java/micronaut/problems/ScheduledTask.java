package micronaut.problems;

import io.micronaut.context.annotation.Parallel;
import io.micronaut.scheduling.annotation.Scheduled;

public class ScheduledTask {
    static int counter = 0;

    //@Parallel(shutdownOnError = false) // this would fix all tests
    @Scheduled(fixedRate = "${property.b}")
    void task() {
        counter++;
    }
}
