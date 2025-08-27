package ponomarev.dev.wedding.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeddingRepository extends JpaRepository<RsvpEntity, Long> {

    Optional<RsvpEntity> findByRsvpKey(String key);

}
