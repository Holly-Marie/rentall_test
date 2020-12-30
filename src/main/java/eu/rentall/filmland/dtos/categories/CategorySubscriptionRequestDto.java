package eu.rentall.filmland.dtos.categories;

import lombok.Data;

/**
 * Data transfer object containing the data to subscribe to category.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 10:37
 */
@Data
public class CategorySubscriptionRequestDto {
  /**
   * Email address of the user making the request. Must match the authenticated user.
   */
  private String email;

  /**
   * The category to subscribe to.
   */
  // TODO consider shortening the property name to just 'category', since only available categories can be subscribed to
  private String availableCategory;
}
