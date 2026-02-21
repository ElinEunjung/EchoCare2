package no.kristiania.echocare.playlist.repository;

import no.kristiania.echocare.playlist.domain.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SongRepo extends JpaRepository<Song, UUID> {
}
