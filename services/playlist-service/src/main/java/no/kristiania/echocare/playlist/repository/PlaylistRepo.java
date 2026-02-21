package no.kristiania.echocare.playlist.repository;

import no.kristiania.echocare.playlist.domain.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlaylistRepo extends JpaRepository<Playlist, UUID> {
}
