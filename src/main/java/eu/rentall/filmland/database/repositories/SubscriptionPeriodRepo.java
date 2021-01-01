package eu.rentall.filmland.database.repositories;

import eu.rentall.filmland.database.entities.SubscriptionPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
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
 * Created 31-12-2020 13:09
 */
public interface SubscriptionPeriodRepo extends JpaRepository<SubscriptionPeriodEntity, Integer> {

  List<SubscriptionPeriodEntity> findAllByEndDateBetween(LocalDate begin, LocalDate end);
}
