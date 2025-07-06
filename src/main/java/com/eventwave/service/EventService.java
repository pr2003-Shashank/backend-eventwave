package com.eventwave.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eventwave.dto.ApiResponse;
import com.eventwave.dto.EventCreateRequest;
import com.eventwave.dto.EventDetailDTO;
import com.eventwave.dto.EventSummaryDTO;
import com.eventwave.exception.ApiException;
import com.eventwave.model.Category;
import com.eventwave.model.Event;
import com.eventwave.model.Registration;
import com.eventwave.model.User;
import com.eventwave.repository.CategoryRepository;
import com.eventwave.repository.EventRepository;
import com.eventwave.repository.FavoriteRepository;
import com.eventwave.repository.RegistrationRepository;
import com.eventwave.repository.UserRepository;

@Service
public class EventService {
	
	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private RegistrationRepository registrationRepository;

	@Autowired
	private FavoriteRepository favoriteRepository;
	
	@Autowired
	private S3Service s3Service;
	
	public ApiResponse createEvent(String email, EventCreateRequest request) {
		
		// Fetch user
		User organizer = userRepository.findByEmail(email)
				.orElseThrow(()-> new ApiException("error", "User not found"));
		
		// Check if user has role "ORGANIZER"
		if (!organizer.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("ORGANIZER"))) {
        	throw new ApiException("failed","Only organizers can create events.");
        }
		
		// Fetch category
		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new ApiException("error", "Category not found"));
		
		// Upload image
		String imageUrl = null;
		MultipartFile image = request.getImage();
		if(image!=null && !image.isEmpty()) {
			imageUrl = s3Service.uploadImage(image);
		}
		
		// Create event
        Event event = new Event();
        
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setDate(request.getDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setPrice(request.getPrice());
        event.setCapacity(request.getCapacity());
        event.setImageUrl(imageUrl);
        event.setCategory(category);
        event.setOrganizer(organizer);

        eventRepository.save(event);

        return new ApiResponse("success", "Event created successfully");
    }
	
	public List<EventSummaryDTO> getAllEvents(String emailOrNull) {
	    List<Event> events = eventRepository.findAll();

	    return events.stream().map(event -> {
	        EventSummaryDTO dto = new EventSummaryDTO();
	        dto.setId(event.getId());
	        dto.setTitle(event.getTitle());
	        dto.setDescription(event.getDescription());
	        dto.setDate(event.getDate());
	        dto.setLocation(event.getLocation());
	        dto.setImageUrl(event.getImageUrl());
		    dto.setCategoryName(event.getCategory().getName());
	        
	        int registeredCount = registrationRepository.countByEventId(event.getId());   
		    dto.setRegisteredSeats(registeredCount);
	        dto.setAvailableSeats(event.getCapacity() - registeredCount);
	        
	        if (emailOrNull != null) {
		        User user = userRepository.findByEmail(emailOrNull)
		                .orElseThrow(() -> new RuntimeException("User not found"));
		        dto.setRegistered(registrationRepository.existsByUserIdAndEventId(user.getId(), event.getId()));
		        dto.setFavorite(favoriteRepository.existsByUserIdAndEventId(user.getId(), event.getId()));
		    }

	        return dto;
	    }).collect(Collectors.toList());
	}
	
	public List<EventSummaryDTO> getMyEvents(String email) {
	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new ApiException("error", "User not found"));

	    boolean isOrganizer = user.getRoles().stream()
	            .anyMatch(role -> role.getName().equalsIgnoreCase("ORGANIZER"));

	    List<Event> events;

	    if (isOrganizer) {
	        // Fetch events hosted by this organizer
	        events = eventRepository.findByOrganizer(user);
	    } else {
	        // Fetch events registered by the user
	        List<Registration> registrations = registrationRepository.findByUser(user);
	        events = registrations.stream()
	                .map(Registration::getEvent)
	                .collect(Collectors.toList());
	    }

	    return events.stream().map(event -> {
	    	 EventSummaryDTO dto = new EventSummaryDTO();
		        dto.setId(event.getId());
		        dto.setTitle(event.getTitle());
		        dto.setDescription(event.getDescription());
		        dto.setDate(event.getDate());
		        dto.setLocation(event.getLocation());
		        dto.setImageUrl(event.getImageUrl());
			    dto.setCategoryName(event.getCategory().getName());
		        
		        int registeredCount = registrationRepository.countByEventId(event.getId());   
		        dto.setRegisteredSeats(registeredCount);
		        dto.setAvailableSeats(event.getCapacity() - registeredCount);
		        
		        if (isOrganizer) {
			        dto.setRegistered(false);
			        dto.setFavorite(false);
			    }
		        else {
				    dto.setRegistered(registrationRepository.existsByUserIdAndEventId(user.getId(), event.getId()));
				    dto.setFavorite(favoriteRepository.existsByUserIdAndEventId(user.getId(), event.getId()));
		        }
		        return dto;
		    }).collect(Collectors.toList());
	}

	
	public EventDetailDTO getEventById(Long eventId, String emailOrNull) {
		Event event = eventRepository.findById(eventId)
						.orElseThrow(() -> new ApiException("error", "Event not found"));
		
		EventDetailDTO dto = new EventDetailDTO();
	    dto.setId(event.getId());
	    dto.setTitle(event.getTitle());
	    dto.setDescription(event.getDescription());
	    dto.setDate(event.getDate());
	    dto.setStartTime(event.getStartTime());
	    dto.setEndTime(event.getEndTime());
	    dto.setLocation(event.getLocation());
	    dto.setLatitude(event.getLatitude());
	    dto.setLongitude(event.getLongitude());
	    dto.setImageUrl(event.getImageUrl());
	    dto.setPrice(event.getPrice());
	    dto.setCapacity(event.getCapacity());

	    int registeredCount = registrationRepository.countByEventId(event.getId());
	    dto.setAvailableSeats(event.getCapacity() - registeredCount);

	    dto.setCategoryName(event.getCategory().getName());

	    EventDetailDTO.OrganizerDTO organizerDTO = new EventDetailDTO.OrganizerDTO();
	    organizerDTO.setFullName(event.getOrganizer().getFullName());
	    organizerDTO.setEmail(event.getOrganizer().getEmail());
	    organizerDTO.setUsername(event.getOrganizer().getUsername());
	    dto.setOrganizer(organizerDTO);

	    if (emailOrNull != null) {
	        User user = userRepository.findByEmail(emailOrNull)
	                .orElseThrow(() -> new RuntimeException("User not found"));
	        dto.setRegistered(registrationRepository.existsByUserIdAndEventId(user.getId(), event.getId()));
	        dto.setFavorite(favoriteRepository.existsByUserIdAndEventId(user.getId(), event.getId()));
	    }

	    return dto;
	}
	
	public ApiResponse updateEvent(Long eventId, String email, EventCreateRequest request) {
	    // Fetch user
	    User organizer = userRepository.findByEmail(email)
	            .orElseThrow(() -> new ApiException("error", "User not found"));

	    // Check role
	    if (!organizer.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("ORGANIZER"))) {
	        throw new ApiException("failed", "Only organizers can update events.");
	    }

	    // Fetch event
	    Event event = eventRepository.findById(eventId)
	            .orElseThrow(() -> new ApiException("error", "Event not found"));

	    // Only creator can update
	    if (!event.getOrganizer().getId().equals(organizer.getId())) {
	        throw new ApiException("forbidden", "You are not allowed to edit this event.");
	    }

	    // Optional: upload new image if present
	    String imageUrl = event.getImageUrl(); // retain old if not updated
	    MultipartFile newImage = request.getImage();
	    if (newImage != null && !newImage.isEmpty()) {
	        imageUrl = s3Service.uploadImage(newImage);
	    }

	    // Fetch category
	    Category category = categoryRepository.findById(request.getCategoryId())
	            .orElseThrow(() -> new ApiException("error", "Category not found"));

	    // Update fields
	    event.setTitle(request.getTitle());
	    event.setDescription(request.getDescription());
	    event.setLocation(request.getLocation());
	    event.setLatitude(request.getLatitude());
	    event.setLongitude(request.getLongitude());
	    event.setDate(request.getDate());
	    event.setStartTime(request.getStartTime());
	    event.setEndTime(request.getEndTime());
	    event.setPrice(request.getPrice());
	    event.setCapacity(request.getCapacity());
	    event.setImageUrl(imageUrl);
	    event.setCategory(category);

	    eventRepository.save(event);
	    return new ApiResponse("success", "Event updated successfully");
	}


	public ApiResponse deleteEvent(Long eventId, String email) {
	    User organizer = userRepository.findByEmail(email)
	            .orElseThrow(() -> new ApiException("error", "User not found"));

	    if (!organizer.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("ORGANIZER"))) {
	        throw new ApiException("failed", "Only organizers can delete events.");
	    }

	    Event event = eventRepository.findById(eventId)
	            .orElseThrow(() -> new ApiException("error", "Event not found"));

	    if (!event.getOrganizer().getId().equals(organizer.getId())) {
	        throw new ApiException("forbidden", "You are not allowed to delete this event.");
	    }

	    eventRepository.delete(event);
	    return new ApiResponse("success", "Event deleted successfully");
	}

}
