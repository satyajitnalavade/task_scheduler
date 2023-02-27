package com.example.task_scheduler.repository;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  //  @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Message m WHERE m.id = :id")
    Optional<Message> findByIdForUpdate(@Param("id") Long aLong);


  //  @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Message> findByTriggerTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, MessageStatus status);

  //  @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Message> findByTriggerTimeBeforeAndStatus(LocalDateTime end, MessageStatus status);


}