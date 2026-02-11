package com.shorter.urlshortner.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.shorter.urlshortner.entity.ShortUrl;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCodeIgnoreCase(String shortCode);

    boolean existsByShortCodeIgnoreCase(String shortCode);

    @Modifying
    @Query("DELETE FROM ShortUrl s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < ?1")
    int deleteExpired(Instant before);

    @Query("SELECT s FROM ShortUrl s WHERE s.expiresAt IS NULL OR s.expiresAt > ?1")
    List<ShortUrl> findAllNonExpired(Instant now);


    

}
