package com.eventostec.api.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.repositories.EventRepository;

@Service
public class EventService {

	@Autowired
	private StorageService storageService;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private AddressService addressService;

	public Event createEvent(EventRequestDTO data) {
		String imgUrl = null;

		if(data.image() != null) {
			imgUrl = this.uploadImg(data.image());
		}

		Event newEvent = new Event();

		newEvent.setTitle(data.title());
		newEvent.setDescription(data.description());
		newEvent.setRemote(data.remote());
		newEvent.setDate(new Date(data.date()));
		newEvent.setImgUrl(imgUrl);
		newEvent.setEventUrl(data.eventUrl());

		this.eventRepository.save(newEvent);

		if(!data.remote()) {
			this.addressService.createAddress(data, newEvent);
		}

		return newEvent;
	}

	public List<EventResponseDTO> getUpcomingEvents(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<Event> eventsPage = this.eventRepository.findUpcomingEvents(new Date(), pageable);

		return eventsPage.map(event -> new EventResponseDTO(
				event.getId(), 
				event.getTitle(), 
				event.getDescription(), 
				event.getDate(),
				event.getAddress() != null ? event.getAddress().getCity() : "",
				event.getAddress() != null ? event.getAddress().getUf() : "",
				event.getRemote(),
				event.getEventUrl(),
				event.getImgUrl()))
				.stream().toList();

	}


	private String uploadImg(MultipartFile multipartFile) {
		String imgName = UUID.randomUUID() + "-" + multipartFile.getName();

		try {
			this.storageService.uploadFile("event-bucket", imgName, multipartFile.getInputStream(), multipartFile.getContentType());
			return imgName;
		} catch (Exception e) {
			System.out.println("Erro ao subir arquivo");
			return null;
		}
	}
	
	private Date getTenYearsFromNow() {
	    Calendar calendar = Calendar.getInstance();
	    calendar.add(Calendar.YEAR, 10);
	    return calendar.getTime();
	}


	public List<EventResponseDTO> getFilteredEvents(int page, int size, String title, String city, String uf,
			Date startDate, Date endDate) {
		
		title = (title != null) ? title : "";
		city = (city != null) ? city : "";
		uf = (uf != null) ? uf : "";
		startDate = (startDate != null) ? startDate : new Date();
		endDate = (endDate != null) ? endDate : getTenYearsFromNow();
		
		Pageable pageable = PageRequest.of(page, size);

		Page<Event> eventsPage = this.eventRepository.findFilteredEvents(title, city, uf, startDate, endDate, pageable);


		return eventsPage.map(event -> new EventResponseDTO(
				event.getId(), 
				event.getTitle(), 
				event.getDescription(), 
				event.getDate(),
				event.getAddress() != null ? event.getAddress().getCity() : "",
				event.getAddress() != null ? event.getAddress().getUf() : "",
				event.getRemote(),
				event.getEventUrl(),
				event.getImgUrl()))
				.stream().toList();

	}


}
