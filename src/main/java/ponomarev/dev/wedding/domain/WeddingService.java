package ponomarev.dev.wedding.domain;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ponomarev.dev.wedding.api.GuestDto;
import ponomarev.dev.wedding.api.RsvpDto;
import ponomarev.dev.wedding.db.RsvpEntity;
import ponomarev.dev.wedding.db.WeddingRepository;
import ponomarev.dev.wedding.utilyty.MapperRsvp;

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
}
