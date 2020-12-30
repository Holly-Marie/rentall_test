package eu.rentall.filmland.restapi;

import eu.rentall.filmland.database.repositories.CategoryRepo;
import eu.rentall.filmland.dtos.categories.AvailableAndSubscribedToCategoriesDto;
import eu.rentall.filmland.dtos.categories.CategoryDto;
import eu.rentall.filmland.dtos.categories.CategorySharingRequestDto;
import eu.rentall.filmland.dtos.categories.CategorySubscriptionRequestDto;
import eu.rentall.filmland.dtos.common.ResponseDto;
import eu.rentall.filmland.exceptions.AlreadySubscribedException;
import eu.rentall.filmland.exceptions.CategoryNotFoundException;
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
import java.util.List;
import java.util.stream.Collectors;

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

  private final CategoryRepo categoryRepo;
  private final SubscriptionService subscriptionService;

  public CategoriesRestController(CategoryRepo categoryRepo, SubscriptionService subscriptionService) {
    this.categoryRepo = categoryRepo;
    this.subscriptionService = subscriptionService;
  }

  /**
   * Get all the available categories and the subscribed categories of the authenticated subscriber.
   * @return all available categories and the categories the authenticated user is subscribed to
   */
  @GetMapping("")
  public ResponseEntity<AvailableAndSubscribedToCategoriesDto> getAllAvailableAndSubscribedToCategories(@AuthenticationPrincipal KeycloakPrincipal<?> principal) {
    String email = unwrapEmail(principal);
    log.error("getAllAvailableAndSubscribedToCategories(user = {})", email);


    List<CategoryDto> availableCategories = this.categoryRepo.findAll().stream().map(c -> new CategoryDto(c.getName(), c.getAvailableContent(), c.getPrice())).collect(Collectors.toList());

    return new ResponseEntity<>(new AvailableAndSubscribedToCategoriesDto(availableCategories, null), HttpStatus.OK);
  }

  /**
   * Subscribe to a category.
   * @return the result of the subscription attempt
   */
  @PostMapping("subscribe")
  public ResponseEntity<ResponseDto> subscribeToCategory(@Valid @RequestBody CategorySubscriptionRequestDto subscriptionRequest, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
    String email = unwrapEmail(principal);
    if(!email.equalsIgnoreCase(subscriptionRequest.getEmail())) {
      log.debug("Given email ''{}'' does not match email of authenticated user.", subscriptionRequest.getEmail());
      return new ResponseEntity<>(new ResponseDto("failed", "Given email '%s' does not match email of authenticated user.", subscriptionRequest.getEmail()), HttpStatus.BAD_REQUEST);
    }

    String category = subscriptionRequest.getAvailableCategory();
    try {
      subscriptionService.subscribe(subscriptionRequest.getEmail(), category);
      log.info("User with email ''{}'' subscribed to category ''{}''.", subscriptionRequest.getEmail(), category);
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
    String email = unwrapEmail(principal);
    // TODO implement business logic

    boolean success = false;
    if(success) {
      return new ResponseEntity<>(new ResponseDto("successful", "Successfully shared category: '%s' with user: '%s'.",
          sharingRequest.getSubscribedCategory(), sharingRequest.getCustomer()), HttpStatus.ACCEPTED);
    } else {
      return new ResponseEntity<>(new ResponseDto("failed", "Sharing category '%s' with user: '%s' was refused, " +
          "since the user to share with does not exist.",
          sharingRequest.getSubscribedCategory(), sharingRequest.getCustomer()), HttpStatus.FORBIDDEN);
//      return new ResponseEntity<>(new ResponseDto("failed", "Sharing category '%s' with user: '%s' was refused, " +
//          "since the authenticated user is not subscribed to that category.",
//          sharingRequest.getSubscribedCategory(), sharingRequest.getCustomer()), HttpStatus.FORBIDDEN);
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
}
