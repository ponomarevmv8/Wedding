package ponomarev.dev.wedding.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;

@Data
@RequiredArgsConstructor
public class VideoStreamResponse {
    private final Resource resource;
    private final long start;
    private final long end;
    private final long contentLength;
    private final long fileSize;
    private final boolean isRangeRequest;

}
