package ponomarev.dev.wedding.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ponomarev.dev.wedding.domain.WeddingService;
import ponomarev.dev.wedding.utilyty.MapperRsvp;

@RestController
@RequestMapping("/api/rsvp")
@RequiredArgsConstructor
public class WeddingController {

    private final WeddingService weddingService;
    private final MapperRsvp mapperRsvp;

    @PostMapping()
    public ResponseEntity<?> registration(@RequestBody RsvpDto rsvpDto) {
        if(validateRspv(rsvpDto)) {
            return ResponseEntity.badRequest().build();
        }
        var answer = weddingService.registrationUser(rsvpDto);
        return ResponseEntity.ok(mapperRsvp.toDto(answer));
    }

    private boolean validateRspv(RsvpDto rsvpDto) {
        return rsvpDto.answer() == null || rsvpDto.name() == null;
    }

    @GetMapping("/guests")
    public ResponseEntity<?> getGuests() {
        return ResponseEntity.ok(weddingService.getGuests());
    }
}
