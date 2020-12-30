package eu.rentall.filmland.dtos.categories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data transfer object representing a category a logged in user is subscribed to.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 10:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class SubscribedCategoryDto extends CategoryDto {
  private LocalDate startDate;

  public SubscribedCategoryDto(String name, int availableContent, BigDecimal price, LocalDate startDate) {
    super(name, availableContent, price);
    this.startDate = startDate;
  }
}
