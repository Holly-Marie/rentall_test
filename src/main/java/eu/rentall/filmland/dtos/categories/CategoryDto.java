package eu.rentall.filmland.dtos.categories;

import eu.rentall.filmland.database.projections.CategoryDtoProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data transfer object representing a category.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 10:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto implements CategoryDtoProjection {
  private String name;
  private int availableContent;
  /* use BigDecimal since this holds a currency and precision and correct representation are paramount */
  private BigDecimal price;
}
