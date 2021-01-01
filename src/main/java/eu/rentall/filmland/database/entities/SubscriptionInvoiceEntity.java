package eu.rentall.filmland.database.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Database entity representing an invoice to a subscription period.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 31-12-2020 12:33
 */
@Entity
@Table(name = "subscription_invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SubscriptionInvoiceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  @EqualsAndHashCode.Include
  private int id;

  @Min(0)
  @Column(nullable = false)
  private BigDecimal price;

  @Column(nullable = false)
  private LocalDate date;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private UserEntity subscriber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private SubscriptionPeriodEntity subscriptionPeriod;
}
