package eu.rentall.filmland.database.entities;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Database entity representing a user.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 11:32
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  @EqualsAndHashCode.Include
  private int id;

  @Length(max = 64)
  @Column(length = 64, unique = true)
  private String userName;

  @Length(max = 265) @Email
  @Column(length = 256, nullable = false, unique = true)
  private String email;

  @OneToMany(mappedBy = "subscriber", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<CategorySubscriptionEntity> subscriptions = new HashSet<>();
}
