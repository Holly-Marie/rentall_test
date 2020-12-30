package eu.rentall.filmland.services;

import eu.rentall.filmland.database.entities.CategoryEntity;
import eu.rentall.filmland.database.entities.CategorySubscriptionEntity;
import eu.rentall.filmland.database.entities.UserEntity;
import eu.rentall.filmland.database.repositories.CategoryRepo;
import eu.rentall.filmland.database.repositories.CategorySubscriptionRepo;
import eu.rentall.filmland.database.repositories.UserRepo;
import eu.rentall.filmland.exceptions.AlreadySubscribedException;
import eu.rentall.filmland.exceptions.CategoryNotFoundException;
import eu.rentall.filmland.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Optional;

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

  public SubscriptionService(UserRepo userRepo, CategoryRepo categoryRepo, CategorySubscriptionRepo categorySubscriptionRepo) {
    this.userRepo = userRepo;
    this.categoryRepo = categoryRepo;
    this.categorySubscriptionRepo = categorySubscriptionRepo;
  }

  @Transactional
  public void subscribe(@NotBlank String userEmail, @NotBlank String categoryName) throws UserNotFoundException, CategoryNotFoundException, AlreadySubscribedException {
    Optional<UserEntity> userOpt = userRepo.findByEmailIgnoreCase(userEmail);
    UserEntity userEntity = userOpt.orElseThrow(() -> new UserNotFoundException(String.format("Could not find user with email: %s", userEmail)));

    Optional<CategoryEntity> categoryOpt = categoryRepo.findByNameIgnoreCase(categoryName);
    CategoryEntity categoryEntity = categoryOpt.orElseThrow(() -> new CategoryNotFoundException(String.format("Could not find category with name: %s", categoryName)));

    if(categorySubscriptionRepo.isUserSubscribed(userEmail, categoryName)) {
      throw new AlreadySubscribedException(String.format("User with email: %s is already subscribed to category: %s", userEmail, categoryName));
    }

    CategorySubscriptionEntity subscription = categorySubscriptionRepo.save(CategorySubscriptionEntity.builder().subscriber(userEntity).category(categoryEntity).price(categoryEntity.getPrice()).startDate(LocalDate.of(2020, 10, 10)).build());
    userEntity.getSubscriptions().add(subscription);
    categoryEntity.getSubscriptions().add(subscription);

    // TODO create first subscription period and track downloaded content
  }
}
