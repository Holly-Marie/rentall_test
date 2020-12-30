package eu.rentall.filmland.database.entities;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Database entity representing a category.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 11:40
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CategoryEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  @EqualsAndHashCode.Include
  private int id;

  @Length(max = 128)
  @Column(length = 128, nullable = false)
  private String name;

  @Min(0)
  @Column(nullable = false)
  private int availableContent;

  @Min(0)
  @Column(nullable = false)
  private BigDecimal price;

  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
  private Set<CategorySubscriptionEntity> subscriptions = new HashSet<>();
}
