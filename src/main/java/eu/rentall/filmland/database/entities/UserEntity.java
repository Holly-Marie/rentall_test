package eu.rentall.filmland.database.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;

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
public class UserEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private int id;

  @Length(max = 64)
  @Column(length = 64, nullable = false)
  private String userName;

  @Length(max = 265) @Email
  @Column(length = 256, nullable = false)
  private String email;
}
