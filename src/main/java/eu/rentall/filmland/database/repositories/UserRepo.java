package eu.rentall.filmland.database.repositories;

import eu.rentall.filmland.database.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA repository for reading and manipulating UserEntities.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 14:09
 * @see JpaRepository
 * @see UserEntity
 */
public interface UserRepo extends JpaRepository<UserEntity, Integer> {
  Optional<UserEntity> findByEmailIgnoreCase(String email);
}
