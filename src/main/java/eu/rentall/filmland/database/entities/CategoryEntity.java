package eu.rentall.filmland.database.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

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
public class CategoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
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
}
