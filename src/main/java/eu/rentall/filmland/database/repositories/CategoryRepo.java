package eu.rentall.filmland.database.repositories;

import eu.rentall.filmland.database.entities.CategoryEntity;
import eu.rentall.filmland.database.projections.CategoryDtoProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for reading and manipulating CategoryEntities.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 14:10
 * @see JpaRepository
 * @see CategoryEntity
 */
public interface CategoryRepo extends JpaRepository<CategoryEntity, Integer> {

  Optional<CategoryEntity> findByNameIgnoreCase(String name);

  List<CategoryDtoProjection> findAllByIdNotIn(Collection<Integer> ids);
}
