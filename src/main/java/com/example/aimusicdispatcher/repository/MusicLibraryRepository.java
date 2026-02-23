package com.example.aimusicdispatcher.repository;

import com.example.aimusicdispatcher.entity.MusicLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicLibraryRepository extends JpaRepository<MusicLibrary, Long> {
    Optional<MusicLibrary> findBySongName(String songName);

    Optional<MusicLibrary> findFirstByOrderByLastPlayedAtAsc();
}
