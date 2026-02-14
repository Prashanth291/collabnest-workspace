package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.DirectChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DirectChatRepository extends JpaRepository<DirectChat, UUID> {

    @Query("SELECT dc FROM DirectChat dc WHERE " +
            "(dc.userOne.id = :userOneId AND dc.userTwo.id = :userTwoId) OR " +
            "(dc.userOne.id = :userTwoId AND dc.userTwo.id = :userOneId)")
    Optional<DirectChat> findByUsers(@Param("userOneId") UUID userOneId,
            @Param("userTwoId") UUID userTwoId);

    @Query("SELECT dc FROM DirectChat dc WHERE dc.userOne.id = :userId OR dc.userTwo.id = :userId")
    List<DirectChat> findByUserId(@Param("userId") UUID userId);
}
