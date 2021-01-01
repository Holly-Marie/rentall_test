package eu.rentall.filmland.database.repositories;

import eu.rentall.filmland.database.entities.SubscriptionInvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for reading and manipulating SubscriptionInvoiceEntities.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 31-12-2020 13:10
 * @see JpaRepository
 * @see SubscriptionInvoiceEntity
 */
public interface SubscriptionInvoiceRepo extends JpaRepository<SubscriptionInvoiceEntity, Integer> {
}
