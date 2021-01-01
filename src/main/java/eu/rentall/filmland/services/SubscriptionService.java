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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
@Slf4j
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

    final CategoryEntity categoryEntity = getCategoryIfUserIsNotAlreadySubscribedToIt(userEmail, categoryName);

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

    final SubscriptionPeriodEntity firstPeriod = createSubscriptionPeriod(subscription, subscription.getStartDate());

    createInvoicesForSubscriptionPeriod(firstPeriod);
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

    final CategoryEntity categoryEntity = getCategoryIfUserIsNotAlreadySubscribedToIt(otherEmail, categoryName);

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
    final LocalDate today = LocalDate.now();
    // just load all ids to be renewed since using spring jpa paging may not work correctly,
    // because we are updating the items and that may influence their order in the page,
    // which may lead to subscriptions being skipped
    final List<Integer> subscriptionIds = categorySubscriptionRepo.findIdsOfSubscriptionsNeedingToBeRenewed(today.plusDays(3));
    if(subscriptionIds == null || subscriptionIds.size() < 1) {
      log.info("no category subscriptions to renew");
      return;
    }
    log.info("{} category subscription(s) to renew", subscriptionIds.size());

    // process max 200 subscriptions at a time
    List<List<Integer>> pages = ListUtils.partition(subscriptionIds, 200);

    pages.forEach(page -> {
      final List<CategorySubscriptionEntity> subscriptions = categorySubscriptionRepo.findAllById(page);

      subscriptions.forEach(subscription -> {
        final Optional<SubscriptionPeriodEntity> currentPeriodOpt = subscription.getPeriods().stream().filter(p -> p.getStartDate().isBefore(today) && p.getEndDate().isAfter(today)).findFirst();
        if(currentPeriodOpt.isEmpty()) {
          log.warn("Can not renew category subscription (id={}, category={}), since it has no current subscription period! Skipping it! " +
              "This should never happen unless some one messed with the database directly.", subscription.getId(), subscription.getCategory().getName());
          return; // skip if there is no current subscription period, this should never happen unless some one messed with the database directly
        }
        log.info("renewing subscription (id={}, category={})", subscription.getId(), subscription.getCategory().getName());
        final SubscriptionPeriodEntity currentPeriod = currentPeriodOpt.get();
        final SubscriptionPeriodEntity nextPeriod = createSubscriptionPeriod(subscription, currentPeriod.getEndDate().plusDays(1));
        createInvoicesForSubscriptionPeriod(nextPeriod);
      });
      categorySubscriptionRepo.flush(); // flush the current page
    });
  }

  private CategoryEntity getCategoryIfUserIsNotAlreadySubscribedToIt(@NotBlank String otherEmail, @NotBlank String categoryName) {
    final Optional<CategoryEntity> categoryOpt = categoryRepo.findByNameIgnoreCase(categoryName);
    final CategoryEntity categoryEntity = categoryOpt.orElseThrow(() -> new CategoryNotFoundException(String.format("Could not find category with name: %s", categoryName)));

    if (categorySubscriptionRepo.isUserSubscribed(otherEmail, categoryName)) {
      throw new AlreadySubscribedException(String.format("User with email: %s is already subscribed to category: %s", otherEmail, categoryName));
    }
    return categoryEntity;
  }

  private SubscriptionPeriodEntity createSubscriptionPeriod(@NotNull CategorySubscriptionEntity subscription, @NotNull LocalDate startDate) {
    final SubscriptionPeriodEntity period = subscriptionPeriodRepo.save(SubscriptionPeriodEntity.builder()
        .subscription(subscription)
        .startDate(startDate)
        .endDate(startDate.plusMonths(1))
        .remainingContent(subscription.getCategory().getAvailableContent())
        .build());
    if (subscription.getPeriods() == null) {
      subscription.setPeriods(Set.of(period));
    } else {
      subscription.getPeriods().add(period);
    }

    return period;
  }

  private void createInvoicesForSubscriptionPeriod(@NotNull SubscriptionPeriodEntity period) {
    CategorySubscriptionEntity subscription = period.getSubscription();
    if(subscription == null) {
      log.warn("Can not create invoice for subscription period (id={}, startDate={}, endDate={}), since it belongs to no subscription! Skipping it! " +
          "This should never happen unless some one messed with the database directly.", period.getId(), period.getStartDate(), period.getEndDate());
      return; // this should never happen unless someone messes with the database directly
    }
    CategoryEntity category = subscription.getCategory();
    if(category == null) {
      log.warn("Can not create invoice for subscription period (id={}, startDate={}, endDate={}), since its subscription belongs to no category! Skipping it! " +
          "This should never happen unless some one messed with the database directly.", period.getId(), period.getStartDate(), period.getEndDate());
      return; // this should never happen unless someone messes with the database directly
    }
    Set<UserEntity> subscribers = subscription.getSubscribers();
    if(subscribers == null || subscribers.size() < 1) {
      log.warn("Can not create invoice for subscription period (id={}, startDate={}, endDate={}), since its subscription has no subscribers! Skipping it! " +
          "This should never happen unless some one messed with the database directly.", period.getId(), period.getStartDate(), period.getEndDate());
      return; // this should never happen unless someone messes with the database directly
    }

    subscribers.forEach(subscriber -> {
      final SubscriptionInvoiceEntity invoice = invoiceRepo.save(SubscriptionInvoiceEntity.builder()
          .date(subscription.getStartDate())
          .price(calculateCurrentSubscriptionPricePerSubscriber(subscription))
          .subscriber(subscriber)
          .subscriptionPeriod(period)
          .build());

      if (subscriber.getInvoices() == null) {
        subscriber.setInvoices(Set.of(invoice));
      } else {
        subscriber.getInvoices().add(invoice);
      }
      if (period.getInvoices() == null) {
        period.setInvoices(Set.of(invoice));
      } else {
        period.getInvoices().add(invoice);
      }
    });
  }

  @NotNull
  private BigDecimal calculateCurrentSubscriptionPricePerSubscriber(@NotNull CategorySubscriptionEntity subscription) {
    if(subscription.getSubscribers() == null || subscription.getSubscribers().size() < 1) {
      throw new IllegalArgumentException("subscription.getSubscribers() must not be null or contain no subscribers");
    }
    if(subscription.getCategory() == null || subscription.getCategory().getPrice() == null || subscription.getCategory().getPrice().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("subscription.getCategory().getPrice() must not be null or be 0 or less");
    }

    BigDecimal pricePerSubscriber = BigDecimal.ZERO;
    if(subscription.getPeriods().size() > 1) {
      // this is not the first subscription period
      MathContext mc = new MathContext(2, RoundingMode.CEILING); // we always round up and may over charge a fraction of a cent but never undercharge
      pricePerSubscriber = subscription.getCategory().getPrice().divide(BigDecimal.valueOf(subscription.getSubscribers().size()), mc);
    }
    return pricePerSubscriber;
  }
}
