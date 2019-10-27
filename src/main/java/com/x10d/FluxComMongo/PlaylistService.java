package com.x10d.FluxComMongo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlaylistService {

	Flux<Playlist> findAll();
	Mono<Playlist> findById(String id);
	Mono<Playlist> save(Playlist p);
	void deletaDepoisCriaDadosNoMongoDB();
}
