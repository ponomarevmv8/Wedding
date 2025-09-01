package ponomarev.dev.wedding.domain;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ponomarev.dev.wedding.api.GuestDto;
import ponomarev.dev.wedding.api.RsvpDto;
import ponomarev.dev.wedding.api.VideoStreamResponse;
import ponomarev.dev.wedding.db.RsvpEntity;
import ponomarev.dev.wedding.db.WeddingRepository;
import ponomarev.dev.wedding.utilyty.MapperRsvp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeddingService {

    private final WeddingRepository weddingRepository;
    private final MapperRsvp mapperRsvp;
    private final Map<String, Integer> answer = new HashMap<>();

    @PostConstruct
    private void init() {
        answer.put("yes", 1);
        answer.put("no", 0);
        answer.put("plus", 2);
    }

    public Rsvp registrationUser(RsvpDto rsvpDto) {
        String key = generateKey(rsvpDto.name());
        var rsvp = weddingRepository.findByRsvpKey(key);
        if(rsvp.isPresent()) {
            var updatedRsvp = rsvp.get();
            updatedRsvp.setName(rsvpDto.name());
            updatedRsvp.setAnswer(rsvpDto.answer());
            return mapperRsvp.toDomain(weddingRepository.save(updatedRsvp));
        }
        var newRsvp = new RsvpEntity();
        newRsvp.setName(rsvpDto.name());
        newRsvp.setAnswer(rsvpDto.answer());
        newRsvp.setRsvpKey(key);
        return mapperRsvp.toDomain(weddingRepository.save(newRsvp));
    }

    public GuestDto getGuests() {
        var rsvpEntities = weddingRepository.findAll();
        if(rsvpEntities.isEmpty()) {
            return new GuestDto(List.of(), 0);
        }
        List<RsvpDto> guests = new ArrayList<>();
        Integer size = 0;
        for(var rsvpEntity : rsvpEntities) {
            if(answer.containsKey(rsvpEntity.getAnswer())) {
                size += answer.get(rsvpEntity.getAnswer());
            }
            guests.add(new RsvpDto(rsvpEntity.getAnswer(), rsvpEntity.getName()));
        }
        return new GuestDto(guests, size);
    }

    private String generateKey(String name) {
        if (name == null) {
            return null;
        }
        return name.replaceAll("[^\\p{L}\\p{N}]", "") // удаляем всё, кроме букв и цифр
                .toLowerCase();
    }

    public VideoStreamResponse streamVideo(String rangeHeader) throws IOException {
        Resource resource = new FileSystemResource("/app/video/video.mp4");

        if(!resource.exists()) {
            throw new IOException("Video file not found");
        }

        long fileSize;
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(resource.getFile(), "r")) {
            fileSize = randomAccessFile.length();
        }

        if(rangeHeader == null) {
            return new VideoStreamResponse(resource, 0, fileSize - 1, fileSize, fileSize, false);
        }

        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        long start;
        try {
            start = Long.parseLong(ranges[0]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Invalid range header");
        }

        long end = Math.min(
                ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileSize - 1,
                fileSize - 1
        );

        if(start > end || start < 0) {
            throw new IllegalArgumentException("Invalid range: start > end or start < 0");
        }

        long contentLength = end - start + 1;

        RandomAccessFile randomAccessFile = new RandomAccessFile(resource.getFile(), "r");
        randomAccessFile.seek(start);

        InputStream inputStream = new FileInputStream(randomAccessFile.getFD()) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                long remaining = end - randomAccessFile.getFilePointer() + 1;
                int bytesToRead = (int) Math.min(len, remaining);
                if (bytesToRead <= 0) {
                    return -1;
                }
                return super.read(b, off, bytesToRead);
            }
        };
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream) {
            @Override
            public long contentLength() {
                return contentLength;
            }
        };

        return new VideoStreamResponse(inputStreamResource, start, end, contentLength, fileSize, true);
    }
}
