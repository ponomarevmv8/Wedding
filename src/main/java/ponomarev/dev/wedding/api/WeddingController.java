package ponomarev.dev.wedding.api;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ponomarev.dev.wedding.domain.WeddingService;
import ponomarev.dev.wedding.utilyty.MapperRsvp;

import java.io.IOException;

@RestController
@RequestMapping("/api/rsvp")
@RequiredArgsConstructor
public class WeddingController {

    private final WeddingService weddingService;
    private final MapperRsvp mapperRsvp;

    @PostMapping()
    public ResponseEntity<?> registration(@RequestBody RsvpDto rsvpDto) {
        if (validateRspv(rsvpDto)) {
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

    @GetMapping("/proposal-video")
    public ResponseEntity<Resource> streamVideo(
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) throws IOException {

        try {
            VideoStreamResponse videoResponce = weddingService.streamVideo(rangeHeader);
            ResponseEntity.BodyBuilder builder = videoResponce.isRangeRequest()
                    ? ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    : ResponseEntity.ok();

            return builder.contentType(MediaType.parseMediaType("video/mp4"))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, videoResponce.isRangeRequest()
                            ? "bytes " + videoResponce.getStart() + "-" + videoResponce.getEnd() + "/" + videoResponce.getFileSize()
                            : null)
                    .contentLength(videoResponce.getContentLength())
                    .body(videoResponce.getResource());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        }

    }

}
