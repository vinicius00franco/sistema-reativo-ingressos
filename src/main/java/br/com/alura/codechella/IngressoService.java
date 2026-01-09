package br.com.alura.codechella;

import io.micrometer.observation.annotation.Observed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class IngressoService {
    @Autowired
    private IngressoRepository repositorio;

    @Autowired
    private VendaRepository vendaRepository;

    @Observed(name = "IngressoService.obterTodos")
    public Flux<IngressoDto> obterTodos() {
        return  repositorio.findAll()
                .map(IngressoDto::toDto);
    }

    @Observed(name = "IngressoService.obterPorId")
    public Mono<IngressoDto> obterPorId(Long id) {
        return  repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(IngressoDto::toDto);
    }

    @Observed(name = "IngressoService.cadastrar")
    public Mono<IngressoDto> cadastrar(IngressoDto dto) {
        return repositorio.save(dto.toEntity())
                .map(IngressoDto::toDto);
    }

    @Observed(name = "IngressoService.excluir")
    public Mono<Void> excluir(Long id) {
        return repositorio.findById(id)
                .flatMap(repositorio::delete);
    }

    @Observed(name = "IngressoService.alterar")
    public Mono<IngressoDto> alterar(Long id, IngressoDto dto) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Id do evento não encontrado.")))
                .flatMap(ingresso -> {
                    ingresso.setEventoId(dto.eventoId());
                    ingresso.setTipo(dto.tipo());
                    ingresso.setValor(dto.valor());
                    ingresso.setTotal(dto.total());
                    return repositorio.save(ingresso);
                })
                .map(IngressoDto::toDto);
    }

    @Transactional
    @Observed(name = "IngressoService.comprar")
    public Mono<IngressoDto> comprar(CompraDto dto) {
        return repositorio.findById(dto.ingressoId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingresso não encontrado.")))
                .flatMap(ingresso -> {
                    return repositorio.findFirstByEventoIdAndTotalGreaterThanOrderByLoteAsc(ingresso.getEventoId(), 0)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum lote disponível para venda.")))
                            .flatMap(activeLote -> {
                                if (!activeLote.getId().equals(ingresso.getId())) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lote não disponível. Compre o lote atual primeiro."));
                                }
                                Venda venda = new Venda();
                                venda.setIngressoId(ingresso.getId());
                                venda.setTotal(dto.total());
                                return vendaRepository.save(venda).then(Mono.defer(() -> {
                                    ingresso.setTotal(ingresso.getTotal() - dto.total());
                                    return repositorio.save(ingresso);
                                }));
                            });
                })
                .map(ingresso -> IngressoDto.toDto(ingresso));
    }

    @Observed(name = "IngressoService.criarLote")
    public Mono<IngressoDto> criarLote(NovoLoteDto dto) {
        return repositorio.findFirstByEventoIdOrderByLoteDesc(dto.eventoId())
                .map(ultimoLote -> ultimoLote.getLote() + 1)
                .defaultIfEmpty(1)
                .flatMap(proximoLote -> {
                    Ingresso novoIngresso = new Ingresso();
                    novoIngresso.setEventoId(dto.eventoId());
                    novoIngresso.setTipo(dto.tipo());
                    novoIngresso.setValor(dto.valor());
                    novoIngresso.setTotal(dto.total());
                    novoIngresso.setLote(proximoLote);
                    return repositorio.save(novoIngresso);
                })
                .map(IngressoDto::toDto);
    }

    @Observed(name = "IngressoService.obterLotesPorEvento")
    public Flux<IngressoDto> obterLotesPorEvento(Long eventoId) {
        return repositorio.findByEventoIdOrderByLoteAsc(eventoId)
                .map(IngressoDto::toDto);
    }
}