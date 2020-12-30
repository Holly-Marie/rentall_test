package eu.rentall.filmland.database.projections;

import java.math.BigDecimal;

/**
 * [Class description.  The first sentence should be a meaningful summary of the class since it
 * will be displayed as the class summary on the Javadoc package page.]
 * <p>
 * [Other notes, including guaranteed invariants, usage instructions and/or examples, reminders
 * about desired improvements, etc.]
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 22:19
 */
public interface CategoryDtoProjection {
  String getName();
  int getAvailableContent();
  BigDecimal getPrice();
}
