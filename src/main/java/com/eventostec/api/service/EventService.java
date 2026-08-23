package com.eventostec.api.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.repositories.EventRepository;

@Service
public class EventService {

	@Autowired
	private StorageService storageService;
	
	@Autowired
	private EventRepository eventRepository;
	
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
		
		return newEvent;
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
}
