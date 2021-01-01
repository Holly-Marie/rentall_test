package eu.rentall.filmland.database;

import eu.rentall.filmland.database.entities.CategoryEntity;
import eu.rentall.filmland.database.entities.UserEntity;
import eu.rentall.filmland.database.repositories.CategoryRepo;
import eu.rentall.filmland.database.repositories.UserRepo;
import eu.rentall.filmland.services.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * This class inserts initial data into the database after start up.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 16:28
 */
@Slf4j
@Component
public class DatabaseInitializer {

  private final UserRepo userRepo;
  private final CategoryRepo categoryRepo;
  private final SubscriptionService subscriptionService;

  public DatabaseInitializer(UserRepo userRepo, CategoryRepo categoryRepo, SubscriptionService subscriptionService) {
    this.userRepo = userRepo;
    this.categoryRepo = categoryRepo;
    this.subscriptionService = subscriptionService;
  }

  @PostConstruct
  public void init(){
    UserEntity userJava = userRepo.save(UserEntity.builder().userName("java").email("java@rent-all.com").build()); // pw: 1234
    UserEntity userKotlin = userRepo.save(UserEntity.builder().userName("kotlin").email("kotlin@rent-all.com").build()); // pw: 1234
    UserEntity userAdmin = userRepo.save(UserEntity.builder().userName("admin").email("admin@rent-all.com").build()); // pw: 1234

    CategoryEntity dutchFilms = categoryRepo.save(CategoryEntity.builder().name("Dutch Films").availableContent(10).price(BigDecimal.valueOf(4.0)).build());
    CategoryEntity dutchSeries = categoryRepo.save(CategoryEntity.builder().name("Dutch Series").availableContent(20).price(BigDecimal.valueOf(6.0)).build());
    CategoryEntity internationalFilms = categoryRepo.save(CategoryEntity.builder().name("International Films").availableContent(5).price(BigDecimal.valueOf(8.0)).build());

    try {
      subscriptionService.subscribe(userJava.getEmail(), internationalFilms.getName(), LocalDate.now().minusDays(28));
    } catch (RuntimeException e) {
      log.warn("subscribing to a a category failed:", e);
    }
  }
}
