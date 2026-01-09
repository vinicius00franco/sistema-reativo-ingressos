package br.com.alura.codechella;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngressoRepository extends ReactiveCrudRepository<Ingresso, Long> {
    Mono<Ingresso> findFirstByEventoIdAndTotalGreaterThanOrderByLoteAsc(Long eventoId, int total);
    Flux<Ingresso> findByEventoIdOrderByLoteAsc(Long eventoId);
    Mono<Ingresso> findFirstByEventoIdOrderByLoteDesc(Long eventoId);
}
