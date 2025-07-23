package br.com.antoniolps.CommunityCenterAPI.scheduler;

import br.com.antoniolps.CommunityCenterAPI.service.CommunityCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityCenterScheduler {

    private final CommunityCenterService communityCenterService;

    @Scheduled(cron = "0 0 */2 * * *")
    public void scheduleMaxOccupancyNotification() {
        communityCenterService.performMaxCapacityNotification();
    }
}
