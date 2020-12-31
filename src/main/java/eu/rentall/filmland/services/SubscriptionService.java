package eu.rentall.filmland.services;

import eu.rentall.filmland.database.entities.*;
import eu.rentall.filmland.database.projections.CategoryDtoProjection;
import eu.rentall.filmland.database.repositories.*;
import eu.rentall.filmland.dtos.categories.AvailableAndSubscribedToCategoriesDto;
import eu.rentall.filmland.dtos.categories.SubscribedCategoryDto;
import eu.rentall.filmland.exceptions.AlreadySubscribedException;
import eu.rentall.filmland.exceptions.CategoryNotFoundException;
import eu.rentall.filmland.exceptions.NotSubscribedException;
import eu.rentall.filmland.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [Class description.  The first sentence should be a meaningful summary of the class since it
 * will be displayed as the class summary on the Javadoc package page.]
 * <p>
 * [Other notes, including guaranteed invariants, usage instructions and/or examples, reminders
 * about desired improvements, etc.]
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 20:04
 */
@Service
public class SubscriptionService {

  private final UserRepo userRepo;
  private final CategoryRepo categoryRepo;
  private final CategorySubscriptionRepo categorySubscriptionRepo;
  private final SubscriptionPeriodRepo subscriptionPeriodRepo;
  private final SubscriptionInvoiceRepo invoiceRepo;

  public SubscriptionService(UserRepo userRepo, CategoryRepo categoryRepo, CategorySubscriptionRepo categorySubscriptionRepo, SubscriptionPeriodRepo subscriptionPeriodRepo, SubscriptionInvoiceRepo invoiceRepo) {
    this.userRepo = userRepo;
    this.categoryRepo = categoryRepo;
    this.categorySubscriptionRepo = categorySubscriptionRepo;
    this.subscriptionPeriodRepo = subscriptionPeriodRepo;
    this.invoiceRepo = invoiceRepo;
  }

  /**
   * Lists the active subscriptions of the current user and the categories, the user is not subscribed to.
   *
   * @param email email address of the authenticated user making the request
   * @return the DTO containing the active subscriptions of the current user and the categories, the user is not subscribed to
   */
  @Transactional(readOnly = true)
  public AvailableAndSubscribedToCategoriesDto getAllAvailableAndSubscribedToCategories(String email) {
    final LocalDate now = LocalDate.now();
    final List<CategorySubscriptionEntity> subscribedCategoryEntities = this.categorySubscriptionRepo.findSubscribedCategories(email);
    final List<SubscribedCategoryDto> subscribedCategories = subscribedCategoryEntities.stream().map(cs -> {
      final CategoryEntity c = cs.getCategory();
      SubscriptionPeriodEntity currentPeriod = cs.getPeriods().stream().filter(p ->
          (p.getStartDate().equals(now) || p.getStartDate().isBefore(now))
              && (p.getEndDate().equals(now) || p.getEndDate().isAfter(now))
      ).findFirst().orElseThrow(() -> new RuntimeException(String.format("No current subscription period found for subscription to category: %s", c.getName())));
      final Set<String> sharingSubscribers = cs.getSubscribers().stream().map(UserEntity::getEmail).collect(Collectors.toSet());
      return new SubscribedCategoryDto(c.getName(), currentPeriod.getRemainingContent(), c.getPrice(), cs.getStartDate(), sharingSubscribers);
    }).collect(Collectors.toList());

    final List<Integer> subscribedCategoryIds = subscribedCategoryEntities.stream().map(cs -> cs.getCategory().getId()).collect(Collectors.toList());
    final List<CategoryDtoProjection> availableCategories = this.categoryRepo.findAllByIdNotIn(subscribedCategoryIds);

    return new AvailableAndSubscribedToCategoriesDto(availableCategories, subscribedCategories);
  }

  /**
   * Subscribe a user to a category.
   *
   * @param userEmail    the user to subscribe
   * @param categoryName the category to subscribe to
   * @throws UserNotFoundException      if no user with the given userEmail is found
   * @throws CategoryNotFoundException  if no category with the given categoryName is found
   * @throws AlreadySubscribedException if the user is already subscribed to that category
   */
  @Transactional
  public void subscribe(@NotBlank String userEmail, @NotBlank String categoryName) throws UserNotFoundException, CategoryNotFoundException, AlreadySubscribedException {
    final Optional<UserEntity> userOpt = userRepo.findByEmailIgnoreCase(userEmail);
    final UserEntity subscriber = userOpt.orElseThrow(() -> new UserNotFoundException(String.format("Could not find user with email: %s", userEmail)));

    final Optional<CategoryEntity> categoryOpt = categoryRepo.findByNameIgnoreCase(categoryName);
    final CategoryEntity categoryEntity = categoryOpt.orElseThrow(() -> new CategoryNotFoundException(String.format("Could not find category with name: %s", categoryName)));

    if (categorySubscriptionRepo.isUserSubscribed(userEmail, categoryName)) {
      throw new AlreadySubscribedException(String.format("User with email: %s is already subscribed to category: %s", userEmail, categoryName));
    }

    final CategorySubscriptionEntity subscription = categorySubscriptionRepo.save(CategorySubscriptionEntity.builder()
        .category(categoryEntity)
        .subscribers(Set.of(subscriber))
        .startDate(LocalDate.now())
        .build());
    if (subscriber.getSubscriptions() == null) {
      subscriber.setSubscriptions(Set.of(subscription));
    } else {
      subscriber.getSubscriptions().add(subscription);
    }
    if (categoryEntity.getSubscriptions() == null) {
      categoryEntity.setSubscriptions(Set.of(subscription));
    } else {
      categoryEntity.getSubscriptions().add(subscription);
    }

    final SubscriptionPeriodEntity firstPeriod = subscriptionPeriodRepo.save(SubscriptionPeriodEntity.builder()
        .subscription(subscription)
        .startDate(subscription.getStartDate())
        .endDate(subscription.getStartDate().plusMonths(1))
        .remainingContent(categoryEntity.getAvailableContent())
        .build());

    final SubscriptionInvoiceEntity firstInvoice = invoiceRepo.save(SubscriptionInvoiceEntity.builder()
        .date(subscription.getStartDate())
        .price(BigDecimal.ZERO)
        .subscriber(subscriber)
        .subscriptionPeriod(firstPeriod)
        .build());

    if (subscriber.getInvoices() == null) {
      subscriber.setInvoices(Set.of(firstInvoice));
    } else {
      subscriber.getInvoices().add(firstInvoice);
    }
    if (firstPeriod.getInvoices() == null) {
      firstPeriod.setInvoices(Set.of(firstInvoice));
    } else {
      firstPeriod.getInvoices().add(firstInvoice);
    }
  }

  /**
   * Share an existing subscription with another existing user.
   *
   * @param ownerEmail   email address of the user who is currently subscribed
   * @param otherEmail   email address of the user to share the subscription with
   * @param categoryName name of the category to share
   * @throws UserNotFoundException      if no user with the given userEmail is found
   * @throws CategoryNotFoundException  if no category with the given categoryName is found
   * @throws AlreadySubscribedException if the user is already subscribed to that category
   */
  @Transactional
  public void shareSubscription(@NotBlank String ownerEmail, @NotBlank String otherEmail, @NotBlank String categoryName)
      throws UserNotFoundException, CategoryNotFoundException, NotSubscribedException, AlreadySubscribedException {
    final Optional<UserEntity> subscriberOpt = userRepo.findByEmailIgnoreCase(ownerEmail);
    final UserEntity subscriber = subscriberOpt.orElseThrow(() -> new UserNotFoundException(String.format("Could not find subscriber with email: %s", ownerEmail)));

    final Optional<UserEntity> userToShareWithOpt = userRepo.findByEmailIgnoreCase(otherEmail);
    final UserEntity userToShareWith = userToShareWithOpt.orElseThrow(() -> new UserNotFoundException(String.format("Could not find other user with email: %s", otherEmail)));

    final Optional<CategoryEntity> categoryOpt = categoryRepo.findByNameIgnoreCase(categoryName);
    final CategoryEntity categoryEntity = categoryOpt.orElseThrow(() -> new CategoryNotFoundException(String.format("Could not find category with name: %s", categoryName)));

    if (categorySubscriptionRepo.isUserSubscribed(otherEmail, categoryName)) {
      throw new AlreadySubscribedException(String.format("User with email: %s is already subscribed to category: %s", otherEmail, categoryName));
    }

    subscriber.getSubscriptions().stream().filter(cs -> cs.getCategory().getId() == categoryEntity.getId()).findFirst().ifPresentOrElse(cs -> {
      cs.getSubscribers().add(userToShareWith);
      if (userToShareWith.getSubscriptions() == null) {
        userToShareWith.setSubscriptions(Set.of(cs));
      } else {
        userToShareWith.getSubscriptions().add(cs);
      }
    }, () -> {
      throw new NotSubscribedException(String.format("User with email: %s is not subscribed to category: %s", ownerEmail, categoryName));
    });
  }

  @Transactional
  public void renewSubscriptions() {

  }
}
