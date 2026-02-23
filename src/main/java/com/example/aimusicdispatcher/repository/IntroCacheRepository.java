package com.example.aimusicdispatcher.repository;

import com.example.aimusicdispatcher.entity.IntroCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IntroCacheRepository extends JpaRepository<IntroCache, Long> {
    Optional<IntroCache> findByMusicId(Long musicId);
}
