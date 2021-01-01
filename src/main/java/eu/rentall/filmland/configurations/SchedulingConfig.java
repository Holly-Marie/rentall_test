package eu.rentall.filmland.configurations;

import eu.rentall.filmland.services.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration for running tasks scheduled.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 31-12-2020 18:32
 */
@Slf4j
@Configuration
@EnableScheduling
public class SchedulingConfig {

  private final SubscriptionService subscriptionService;

  public SchedulingConfig(SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  @Scheduled(cron = "0 4 * * * ?") // run every night at 4 am
  public void renewExpiringSubscriptions() {
    log.info("*** starting to renew and re-bill expiring subscriptions");
    subscriptionService.renewSubscriptions();
    log.info("*** completed renewing and re-billing expiring subscriptions");
  }
}
