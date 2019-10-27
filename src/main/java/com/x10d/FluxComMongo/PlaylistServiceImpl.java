package com.x10d.FluxComMongo;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PlaylistServiceImpl implements PlaylistService {

	@Autowired
	private PlaylistRepository playlistRepository;
	
	@Override
	public Flux<Playlist> findAll() {
		return playlistRepository.findAll();
	}

	@Override
	public Mono<Playlist> findById(String id) {
		return playlistRepository.findById(id);
	}

	@Override
	public Mono<Playlist> save(Playlist playlist) {
		return playlistRepository.save(playlist);
	}
	
	@Override
	public void deletaDepoisCriaDadosNoMongoDB() {
		
		playlistRepository.deleteAll()
						  .thenMany(Flux.just("fer", "lais", "kika", "neide", "maria", "zarinha", "cica", "nanda", "mae")
								  .map(nome -> new Playlist(UUID.randomUUID().toString(), nome))
								  .flatMap(p -> playlistRepository.save(p)))
						  .subscribe(System.out::println);
	}
	
}
