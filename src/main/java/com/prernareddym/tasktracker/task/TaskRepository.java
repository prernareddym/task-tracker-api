package com.prernareddym.tasktracker.task;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("select t.status as status, count(t) as total from Task t where t.ownerId = :ownerId group by t.status")
    List<StatusCount> countByStatus(@Param("ownerId") Long ownerId);

    interface StatusCount {
        TaskStatus getStatus();

        long getTotal();
    }
}
