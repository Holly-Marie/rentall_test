package eu.rentall.filmland.services;

import eu.rentall.filmland.database.entities.*;
import eu.rentall.filmland.database.projections.CategoryDtoProjection;
import eu.rentall.filmland.database.repositories.*;
import eu.rentall.filmland.dtos.categories.AvailableAndSubscribedToCategoriesDto;
import eu.rentall.filmland.dtos.categories.SubscribedCategoryDto;
import eu.rentall.filmland.exceptions.AlreadySubscribedException;
import eu.rentall.filmland.exceptions.CategoryNotFoundException;
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

  @Transactional(readOnly = true)
  public AvailableAndSubscribedToCategoriesDto getAllAvailableAndSubscribedToCategories(String email) {
    List<CategorySubscriptionEntity> subscribedCategoryEntities = this.categorySubscriptionRepo.findSubscribedCategories(email);
    List<SubscribedCategoryDto> subscribedCategories = subscribedCategoryEntities.stream().map(cs -> {
      CategoryEntity c = cs.getCategory();
      LocalDate now = LocalDate.now();
      SubscriptionPeriodEntity currentPeriod = cs.getPeriods().stream().filter(p ->
          (p.getStartDate().equals(now) || p.getStartDate().isBefore(now))
              && (p.getEndDate().equals(now) || p.getEndDate().isAfter(now))
      ).findFirst().orElseThrow(() -> new RuntimeException(String.format("No current subscription period found for subscription to category: %s", c.getName())));
      Set<String> sharingSubscribers = cs.getSubscribers().stream().map(UserEntity::getEmail).collect(Collectors.toSet());
      return new SubscribedCategoryDto(c.getName(), currentPeriod.getRemainingContent(), c.getPrice(), cs.getStartDate(), sharingSubscribers);
    }).collect(Collectors.toList());

    List<Integer> subscribedCategoryIds = subscribedCategoryEntities.stream().map(cs -> cs.getCategory().getId()).collect(Collectors.toList());
    List<CategoryDtoProjection> availableCategories = this.categoryRepo.findAllByIdNotIn(subscribedCategoryIds);

    return new AvailableAndSubscribedToCategoriesDto(availableCategories, subscribedCategories);
  }

  @Transactional
  public void subscribe(@NotBlank String userEmail, @NotBlank String categoryName) throws UserNotFoundException, CategoryNotFoundException, AlreadySubscribedException {
    Optional<UserEntity> userOpt = userRepo.findByEmailIgnoreCase(userEmail);
    UserEntity subscriber = userOpt.orElseThrow(() -> new UserNotFoundException(String.format("Could not find user with email: %s", userEmail)));

    Optional<CategoryEntity> categoryOpt = categoryRepo.findByNameIgnoreCase(categoryName);
    CategoryEntity categoryEntity = categoryOpt.orElseThrow(() -> new CategoryNotFoundException(String.format("Could not find category with name: %s", categoryName)));

    if(categorySubscriptionRepo.isUserSubscribed(userEmail, categoryName)) {
      throw new AlreadySubscribedException(String.format("User with email: %s is already subscribed to category: %s", userEmail, categoryName));
    }

    CategorySubscriptionEntity subscription = categorySubscriptionRepo.save(CategorySubscriptionEntity.builder()
        .category(categoryEntity)
        .subscribers(Set.of(subscriber))
        .startDate(LocalDate.now())
        .build());
    if(subscriber.getSubscriptions() == null) {
      subscriber.setSubscriptions(Set.of(subscription));
    } else {
      subscriber.getSubscriptions().add(subscription);
    }
    if(categoryEntity.getSubscriptions() == null) {
      categoryEntity.setSubscriptions(Set.of(subscription));
    } else {
      categoryEntity.getSubscriptions().add(subscription);
    }

    SubscriptionPeriodEntity firstPeriod = subscriptionPeriodRepo.save(SubscriptionPeriodEntity.builder()
        .subscription(subscription)
        .startDate(subscription.getStartDate())
        .endDate(subscription.getStartDate().plusMonths(1))
        .remainingContent(categoryEntity.getAvailableContent())
        .build());

    SubscriptionInvoiceEntity firstInvoice = invoiceRepo.save(SubscriptionInvoiceEntity.builder()
        .date(subscription.getStartDate())
        .price(BigDecimal.ZERO)
        .subscriber(subscriber)
        .subscriptionPeriod(firstPeriod)
        .build());

    if(subscriber.getInvoices() == null) {
      subscriber.setInvoices(Set.of(firstInvoice));
    } else {
      subscriber.getInvoices().add(firstInvoice);
    }
    if(firstPeriod.getInvoices() == null) {
      firstPeriod.setInvoices(Set.of(firstInvoice));
    } else {
      firstPeriod.getInvoices().add(firstInvoice);
    }
  }
}
