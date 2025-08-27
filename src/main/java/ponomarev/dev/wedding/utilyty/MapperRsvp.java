package ponomarev.dev.wedding.utilyty;

import org.springframework.stereotype.Component;
import ponomarev.dev.wedding.api.RsvpDto;
import ponomarev.dev.wedding.db.RsvpEntity;
import ponomarev.dev.wedding.domain.Rsvp;

@Component
public class MapperRsvp {

    public Rsvp toDomain(RsvpEntity rsvp) {
        return new Rsvp(
                rsvp.getAnswer(),
                rsvp.getName(),
                rsvp.getRsvpKey()
        );
    }

    public RsvpDto toDto(Rsvp rsvp) {
        return new RsvpDto(
                rsvp.answer(),
                rsvp.name()
        );
    }
}
