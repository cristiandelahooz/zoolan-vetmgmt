package com.wornux.data.repository;

import com.wornux.data.entity.Pet;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long>, JpaSpecificationExecutor<Pet> {

  @Query("SELECT p FROM Pet p JOIN p.owners o WHERE o.id = :ownerId AND p.active = true")
  Page<Pet> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

  @Query(
      "SELECT p FROM Pet p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true ORDER BY p.name")
  List<Pet> findSimilarPetsByName(@Param("name") String name);

  @Query("SELECT p FROM Pet p WHERE p.active = true ORDER BY p.name ASC")
  Page<Pet> findByActiveTrueOrderByNameAsc(Pageable pageable);

  @Query("SELECT p FROM Pet p WHERE p.active = true")
  Page<Pet> findAllActive(Pageable pageable);

  @Query("SELECT p FROM Pet p WHERE p.active = true")
  List<Pet> findAllActive();

  @Query("SELECT p FROM Pet p JOIN p.owners o WHERE o.id = :ownerId AND p.active = true")
  List<Pet> findByOwnerId2(@Param("ownerId") Long ownerId);
}
