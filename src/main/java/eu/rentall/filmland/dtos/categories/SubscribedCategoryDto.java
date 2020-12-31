package eu.rentall.filmland.dtos.categories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Data transfer object representing a category a logged in user is subscribed to.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 10:27
 */
@Data
@Builder
@AllArgsConstructor
public class SubscribedCategoryDto {
  @NotBlank
  private String name;
  @Min(0)
  private int remainingContent;
  @Min(0)
  private BigDecimal price;
  private LocalDate startDate;
  private Set<String> sharingSubscribers;
}
