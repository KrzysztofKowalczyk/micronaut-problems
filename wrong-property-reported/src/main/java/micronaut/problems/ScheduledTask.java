package micronaut.problems;

import io.micronaut.scheduling.annotation.Scheduled;

public class ScheduledTask {
    static int counter = 0;

    @Scheduled(fixedRate = "${property.b}")
    void task() {
        counter++;
    }
}
