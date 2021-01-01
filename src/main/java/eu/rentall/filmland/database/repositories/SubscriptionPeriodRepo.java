package eu.rentall.filmland.database.repositories;

import eu.rentall.filmland.database.entities.SubscriptionPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA repository for reading and manipulating SubscriptionPeriodEntities.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 31-12-2020 13:09
 * @see JpaRepository
 * @see SubscriptionPeriodEntity
 */
public interface SubscriptionPeriodRepo extends JpaRepository<SubscriptionPeriodEntity, Integer> {

  List<SubscriptionPeriodEntity> findAllByEndDateBetween(LocalDate begin, LocalDate end);
}
