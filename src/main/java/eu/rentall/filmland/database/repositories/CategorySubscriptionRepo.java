package eu.rentall.filmland.database.repositories;

import eu.rentall.filmland.database.entities.CategorySubscriptionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA repository for reading and manipulating CategorySubscriptionEntities.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 14:11
 * @see JpaRepository
 * @see CategorySubscriptionEntity
 */
public interface CategorySubscriptionRepo extends JpaRepository<CategorySubscriptionEntity, Integer> {

  @Query("SELECT (COUNT(cs.id ) > 0) as subscribed FROM CategorySubscriptionEntity cs inner join cs.subscribers s where LOWER(s.email)=LOWER(:email) AND LOWER(cs.category.name)=LOWER(:category) ")
  boolean isUserSubscribed(@Param("email") String userEmail, @Param("category") String category);


  @EntityGraph(attributePaths = {"category", "subscribers", "periods"})
  @Query("SELECT cs1 FROM CategorySubscriptionEntity cs1 where cs1.id in (SELECT cs.id FROM CategorySubscriptionEntity cs inner join cs.subscribers s where LOWER(s.email)=LOWER(:email))")
  List<CategorySubscriptionEntity> findSubscribedCategories(@Param("email") String userEmail);

  /**
   * Finds category subscriptions which expired in 3 or less days and have not been renewed yet.
   * <p/>
   * It does this by finding category subscriptions which have a (one) current subscription period
   * (startDate before today and endDate 3 or fewer days away)
   * but have no additional subscription periods with a start data more than 3 days in the future.
   * <p/>
   * To keep the method database agnostic no database date calculating functions are used and instead
   * the current date plus 3 days needs to be given as an argument.
   * @param today the current date
   * @param in3days the current date plus 3 days
   * @return a list of subscriptions which need to be renewed
   */
  @EntityGraph(attributePaths = {"category", "subscribers", "periods"})
  @Query("SELECT distinct cs FROM CategorySubscriptionEntity cs where cs.id in " +
      "(SELECT s.id FROM SubscriptionPeriodEntity p inner join p.subscription s " +
      "where p.startDate > :today or (p.endDate > :today and p.endDate <= :in3days) " +
      "group by s.id having count(p.id) = 1)")
  List<CategorySubscriptionEntity> findSubscriptionsNeedingToBeRenewed(@Param("today")LocalDate today, @Param("in3days")LocalDate in3days);

  /**
   * Finds category subscriptions which expired in 3 or less days and have not been renewed yet.
   * <p/>
   * It does this by finding category subscriptions which have a (one) current subscription period
   * (startDate before today and endDate 3 or fewer days away)
   * but have no additional subscription periods with a start data more than 3 days in the future.
   * <p/>
   * To keep the method database agnostic no database date calculating functions are used and instead
   * the current date plus 3 days needs to be given as an argument.
   * @param today the current date
   * @param in3days the current date plus 3 days
   * @return a list of subscriptions which need to be renewed
   */
  @Query("SELECT distinct cs.id FROM CategorySubscriptionEntity cs where cs.id in " +
      "(SELECT s.id FROM SubscriptionPeriodEntity p inner join p.subscription s " +
      "where p.startDate > :today or (p.endDate > :today and p.endDate <= :in3days) " +
      "group by s.id having count(p.id) = 1)")
  List<Integer> findIdsOfSubscriptionsNeedingToBeRenewed(@Param("today")LocalDate today, @Param("in3days")LocalDate in3days);

}
