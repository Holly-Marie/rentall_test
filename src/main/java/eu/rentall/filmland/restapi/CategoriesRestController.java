package eu.rentall.filmland.restapi;

import eu.rentall.filmland.dtos.categories.AvailableAndSubscribedToCategoriesDto;
import eu.rentall.filmland.dtos.categories.CategorySharingRequestDto;
import eu.rentall.filmland.dtos.categories.CategorySubscriptionRequestDto;
import eu.rentall.filmland.dtos.common.ResponseDto;
import eu.rentall.filmland.exceptions.AlreadySubscribedException;
import eu.rentall.filmland.exceptions.CategoryNotFoundException;
import eu.rentall.filmland.exceptions.NotSubscribedException;
import eu.rentall.filmland.exceptions.UserNotFoundException;
import eu.rentall.filmland.services.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * REST controller for endpoints concerning categories.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 10:17
 */
@Slf4j
@RequestMapping("/categories")
@RestController
public class CategoriesRestController {

  private final SubscriptionService subscriptionService;

  public CategoriesRestController(SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  /**
   * Get all the available categories and the subscribed categories of the authenticated subscriber.
   * @return all available categories and the categories the authenticated user is subscribed to
   */
  @GetMapping("")
  public ResponseEntity<AvailableAndSubscribedToCategoriesDto> getAllAvailableAndSubscribedToCategories(@AuthenticationPrincipal KeycloakPrincipal<?> principal) {
    String email = unwrapEmail(principal);

    return new ResponseEntity<>(subscriptionService.getAllAvailableAndSubscribedToCategories(email), HttpStatus.OK);
  }

  /**
   * Subscribe to a category.
   * @return the result of the subscription attempt
   */
  @PostMapping("subscribe")
  public ResponseEntity<ResponseDto> subscribeToCategory(@Valid @RequestBody CategorySubscriptionRequestDto subscriptionRequest, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
    Optional<ResponseEntity<ResponseDto>> errorResponse = checkEmailMatchesAuthenticatedUser(subscriptionRequest.getEmail(), principal);
    if(errorResponse.isPresent()) {
      return errorResponse.get();
    }

    String category = subscriptionRequest.getAvailableCategory();
    try {
      subscriptionService.subscribe(subscriptionRequest.getEmail(), category);
      log.info("User with email '{}' subscribed to category '{}'.", subscriptionRequest.getEmail(), category);
      return new ResponseEntity<>(new ResponseDto("successful", "Successfully subscribed to category: %s.", subscriptionRequest.getAvailableCategory()), HttpStatus.ACCEPTED);
    } catch (UserNotFoundException | CategoryNotFoundException e) {
      return new ResponseEntity<>(new ResponseDto("failed", "Subscription to category '%s' refused. %s", category, e.getMessage()), HttpStatus.NOT_FOUND);
    } catch (AlreadySubscribedException e) {
      return new ResponseEntity<>(new ResponseDto("failed", "Subscription to category '%s' refused. %s", category, e.getMessage()), HttpStatus.FORBIDDEN);
    }
  }

  /**
   * Share a category the authenticated user is subscribed to with another existing user.
   * @return the result of the share attempt
   */
  @PostMapping("share")
  public ResponseEntity<ResponseDto> shareCategory(@Valid @RequestBody CategorySharingRequestDto sharingRequest, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
    Optional<ResponseEntity<ResponseDto>> errorResponse = checkEmailMatchesAuthenticatedUser(sharingRequest.getEmail(), principal);
    if(errorResponse.isPresent()) {
      return errorResponse.get();
    }

    String category = sharingRequest.getSubscribedCategory();
    try {
      subscriptionService.shareSubscription(sharingRequest.getEmail(), sharingRequest.getCustomer(), category);
      log.info("User with email '{}' shared category '{}' with user with email '{}'.", sharingRequest.getEmail(), category, sharingRequest.getCustomer());
      return new ResponseEntity<>(new ResponseDto("successful", "Successfully shared category: '%s' with user: '%s'.",
          category, sharingRequest.getCustomer()), HttpStatus.ACCEPTED);
    } catch (UserNotFoundException | CategoryNotFoundException e) {
      return new ResponseEntity<>(new ResponseDto("failed", "Sharing category '%s' with user: '%s' was refused. %s",
          category, sharingRequest.getCustomer(), e.getMessage()), HttpStatus.NOT_FOUND);
    } catch (AlreadySubscribedException | NotSubscribedException e) {
      return new ResponseEntity<>(new ResponseDto("failed", "Sharing category '%s' with user: '%s' was refused. %s",
          category, sharingRequest.getCustomer(), e.getMessage()), HttpStatus.FORBIDDEN);
    }
  }

  private @NotNull String unwrapEmail(KeycloakPrincipal<?> principal) {
    try {
      String email = principal.getKeycloakSecurityContext().getToken().getEmail();
      return email == null ? "" : email;
    } catch (Exception e) {
      return "";
    }
  }

  private Optional<ResponseEntity<ResponseDto>> checkEmailMatchesAuthenticatedUser(String requestEmail, KeycloakPrincipal<?> principal) {
    String email = unwrapEmail(principal);
    if(!email.equalsIgnoreCase(requestEmail)) {
      log.debug("Given email '{}' does not match email of authenticated user.", requestEmail);
      return Optional.of(new ResponseEntity<>(new ResponseDto("failed", "Given email '%s' does not match email of authenticated user.", requestEmail), HttpStatus.BAD_REQUEST));
    } else {
      return Optional.empty();
    }
  }
}
