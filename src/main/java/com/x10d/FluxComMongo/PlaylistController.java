package com.x10d.FluxComMongo;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@RestController
public class PlaylistController {

	@Autowired
	private PlaylistService service;
	
	@GetMapping("/pegatodos")
	public Flux<Playlist> pegaTodos() {
		return service.findAll();
	}
	@GetMapping("/pega/{id}")
	public Mono<Playlist> pega(@PathVariable String id) {
		return service.findById(id);
	}
	@PostMapping("/cria")
	public Mono<Playlist> cria(@RequestBody Playlist playlist) {
		return service.save(playlist);
	}
	@PostMapping("/criatodos")
	public void criaTodos() {
		service.deletaDepoisCriaDadosNoMongoDB();
	}
	
	@GetMapping(value="/playlist/events", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Tuple2<Long, Playlist>> getPlaylistByEvent(){
		
		Flux<Long> interval = Flux.interval(Duration.ofSeconds(10));
		
		Flux<Playlist> findAllEvents = service.findAll();
		
		System.out.println("passou pelo endpoint de e evento");
		
		return Flux.zip(interval, findAllEvents);
	}
	
}
