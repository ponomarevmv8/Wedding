package ponomarev.dev.wedding.db;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rsvp")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RsvpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "answer", nullable = false)
    private String answer;

    @Column(name = "rsvp_key", unique = true, nullable = false)
    private String rsvpKey;


}
