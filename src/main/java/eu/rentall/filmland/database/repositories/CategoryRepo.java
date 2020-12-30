package eu.rentall.filmland.database.repositories;

import eu.rentall.filmland.database.entities.CategoryEntity;
import eu.rentall.filmland.database.projections.CategoryDtoProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * [Class description.  The first sentence should be a meaningful summary of the class since it
 * will be displayed as the class summary on the Javadoc package page.]
 * <p>
 * [Other notes, including guaranteed invariants, usage instructions and/or examples, reminders
 * about desired improvements, etc.]
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 14:10
 */
public interface CategoryRepo extends JpaRepository<CategoryEntity, Integer> {

  Optional<CategoryEntity> findByNameIgnoreCase(String name);

  List<CategoryDtoProjection> findAllByIdNotIn(Collection<Integer> ids);
}
