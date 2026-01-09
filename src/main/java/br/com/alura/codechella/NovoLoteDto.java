package br.com.alura.codechella;

public record NovoLoteDto(Long eventoId,
                          TipoIngresso tipo,
                          Double valor,
                          int total) {
}
