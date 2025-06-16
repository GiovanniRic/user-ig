package it.ig.user.domain.repository;

import it.ig.user.domain.model.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {

    @Modifying
    @Query("DELETE FROM RoleEntity r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
}
