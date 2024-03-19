package com.studyolle;

import com.studyolle.domain.Zone;
import com.studyolle.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@RequiredArgsConstructor
public class StudyolleApplication {


	public static void main(String[] args) {
		SpringApplication.run(StudyolleApplication.class, args);
	}

	private final ZoneRepository zoneRepository;
	/*@EventListener(ApplicationReadyEvent.class)
	public void init() throws IOException {
		Resource resource = new ClassPathResource("zones_kr.csv");
		List<Zone> zoneList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
				.map(line -> {
					String[] split = line.split(",");
					return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
				}).collect(Collectors.toList());
		zoneRepository.saveAll(zoneList);
	}*/

}
