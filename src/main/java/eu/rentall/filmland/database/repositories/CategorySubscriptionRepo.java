package eu.rentall.filmland.database.repositories;

import eu.rentall.filmland.database.entities.CategorySubscriptionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * [Class description.  The first sentence should be a meaningful summary of the class since it
 * will be displayed as the class summary on the Javadoc package page.]
 * <p>
 * [Other notes, including guaranteed invariants, usage instructions and/or examples, reminders
 * about desired improvements, etc.]
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 14:11
 */
public interface CategorySubscriptionRepo extends JpaRepository<CategorySubscriptionEntity, Integer> {

  @Query("SELECT (COUNT(cs.id ) > 0) as subscribed FROM CategorySubscriptionEntity cs inner join cs.subscribers s where LOWER(s.email)=LOWER(:email) AND LOWER(cs.category.name)=LOWER(:category) ")
  boolean isUserSubscribed(@Param("email") String userEmail, @Param("category") String category);


  @EntityGraph(attributePaths = {"category", "subscribers", "periods"})
  @Query("SELECT cs1 FROM CategorySubscriptionEntity cs1 where cs1.id in (SELECT cs.id FROM CategorySubscriptionEntity cs inner join cs.subscribers s where LOWER(s.email)=LOWER(:email))")
  List<CategorySubscriptionEntity> findSubscribedCategories(@Param("email") String userEmail);

}
