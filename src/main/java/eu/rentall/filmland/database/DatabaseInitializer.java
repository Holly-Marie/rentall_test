package eu.rentall.filmland.database;

import eu.rentall.filmland.database.entities.CategoryEntity;
import eu.rentall.filmland.database.entities.UserEntity;
import eu.rentall.filmland.database.repositories.CategoryRepo;
import eu.rentall.filmland.database.repositories.CategorySubscriptionRepo;
import eu.rentall.filmland.database.repositories.UserRepo;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

/**
 * [Class description.  The first sentence should be a meaningful summary of the class since it
 * will be displayed as the class summary on the Javadoc package page.]
 * <p>
 * [Other notes, including guaranteed invariants, usage instructions and/or examples, reminders
 * about desired improvements, etc.]
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 16:28
 */
@Component
public class DatabaseInitializer {

  private final UserRepo userRepo;
  private final CategoryRepo categoryRepo;
  private final CategorySubscriptionRepo categorySubscriptionRepo;

  public DatabaseInitializer(UserRepo userRepo, CategoryRepo categoryRepo, CategorySubscriptionRepo categorySubscriptionRepo) {
    this.userRepo = userRepo;
    this.categoryRepo = categoryRepo;
    this.categorySubscriptionRepo = categorySubscriptionRepo;
  }

  @PostConstruct
  public void init(){
    userRepo.save(UserEntity.builder().userName("java").email("java@rent-all.com").build());
    userRepo.save(UserEntity.builder().userName("kotlin").email("kotlin@rent-all.com").build());

    categoryRepo.save(CategoryEntity.builder().name("Dutch Films").availableContent(10).price(BigDecimal.valueOf(4.0)).build());
    categoryRepo.save(CategoryEntity.builder().name("Dutch Series").availableContent(20).price(BigDecimal.valueOf(6.0)).build());
    categoryRepo.save(CategoryEntity.builder().name("International Films").availableContent(40).price(BigDecimal.valueOf(8.0)).build());
  }
}
