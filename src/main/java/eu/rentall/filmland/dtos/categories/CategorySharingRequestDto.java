package eu.rentall.filmland.dtos.categories;

import lombok.Data;

/**
 * Data transfer object containing the data to share a category with another existing user.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 11:10
 */
@Data
public class CategorySharingRequestDto {
  /**
   * Email address of the user making the request. Must match the authenticated user.
   */
  private String email;

  /**
   * Email address of the customer with whom to share the category.
   */
  private String customer;

  /**
   * The category to subscribe to.
   */
  // TODO consider shortening the property name to just 'category', since only subscribed categories can be shared
  private String subscribedCategory;
}
