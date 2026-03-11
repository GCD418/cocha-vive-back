package cocha.vive.backend.repository;

import cocha.vive.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>,
    JpaSpecificationExecutor<Category> {
    Optional<Category> findByName(String name);

    @Modifying
    @Query("UPDATE Category c SET c.isActive = false, " +
        "c.updatedAt = CURRENT_TIMESTAMP, " +
        "c.modifiedByUserId = :userId " +
        "WHERE c.id = :id")
    void softDelete(@Param("id") long id, @Param("userId") long userId);
}
