package ponomarev.dev.wedding.api;

import java.util.List;

public record GuestDto(
        List<RsvpDto> guests,
        Integer size
        ) {
}
