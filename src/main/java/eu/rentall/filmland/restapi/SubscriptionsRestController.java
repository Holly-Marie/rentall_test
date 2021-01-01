package eu.rentall.filmland.restapi;

import eu.rentall.filmland.dtos.common.ResponseDto;
import eu.rentall.filmland.services.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

/**
 * REST controller for endpoints concerning subscriptions.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 1-1-2021 18:52
 */
@Slf4j
@RequestMapping("/subscriptions")
@RestController
public class SubscriptionsRestController {

  private final SubscriptionService subscriptionService;

  public SubscriptionsRestController(SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  @RolesAllowed({"ADMIN", "BACKEND_ADMIN"})
  @GetMapping("renew")
  public ResponseEntity<ResponseDto> renewSubscriptions() {
    int count = this.subscriptionService.renewSubscriptions();

    return new ResponseEntity<>(new ResponseDto("successful", "Renewed %s subscriptions", count), HttpStatus.OK);
  }
}
