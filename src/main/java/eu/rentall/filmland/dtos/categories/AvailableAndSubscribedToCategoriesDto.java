package eu.rentall.filmland.dtos.categories;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Data transfer object containing information about available and subscribed categories.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 10:20
 */
@Data
@AllArgsConstructor
public class AvailableAndSubscribedToCategoriesDto {
  private List<CategoryDto> availableCategories;
  private List<CategoryDto> subscribedCategories;
}
