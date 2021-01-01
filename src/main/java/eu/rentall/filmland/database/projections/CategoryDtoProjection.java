package eu.rentall.filmland.database.projections;

import eu.rentall.filmland.database.entities.CategoryEntity;
import eu.rentall.filmland.database.repositories.CategoryRepo;

import java.math.BigDecimal;

/**
 * Interface used to project some of the fields of the {@link CategoryEntity} when using the {@link CategoryRepo}.
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
