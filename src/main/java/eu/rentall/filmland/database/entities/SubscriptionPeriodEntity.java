package eu.rentall.filmland.database.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Database entity representing a subscription period.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 31-12-2020 12:33
 */
@Entity
@Table(name = "subscription_periods")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SubscriptionPeriodEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  @EqualsAndHashCode.Include
  private int id;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  @Min(0)
  @Column(nullable = false)
  private int remainingContent;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private CategorySubscriptionEntity subscription;

  @OneToMany(mappedBy = "subscriptionPeriod", fetch = FetchType.LAZY)
  private Set<SubscriptionInvoiceEntity> invoices = new HashSet<>();
}
