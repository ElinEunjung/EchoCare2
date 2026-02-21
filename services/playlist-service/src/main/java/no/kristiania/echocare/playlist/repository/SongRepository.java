package no.kristiania.echocare.playlist.repository;

import no.kristiania.echocare.playlist.domain.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SongRepository extends JpaRepository<Song, UUID> {
    List<Song> findByReleaseYearBetween(Integer startYear, Integer endYear);
    List<Song> findByBpmBetween(Double minBpm, Double maxBpm);
    List<Song> findByEnergyBetween(Double minEnergy, Double maxEnergy);
}
