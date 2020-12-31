package eu.rentall.filmland.database.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * [Class description.  The first sentence should be a meaningful summary of the class since it
 * will be displayed as the class summary on the Javadoc package page.]
 * <p>
 * [Other notes, including guaranteed invariants, usage instructions and/or examples, reminders
 * about desired improvements, etc.]
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 11:51
 */
@Entity
@Table(name = "categories_subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CategorySubscriptionEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  @EqualsAndHashCode.Include
  private int id;

  @Column(nullable = false)
  private LocalDate startDate;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<UserEntity> subscribers;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
  private CategoryEntity category;

  @OneToMany(mappedBy = "subscription", fetch = FetchType.LAZY)
  private Set<SubscriptionPeriodEntity> periods;

}
