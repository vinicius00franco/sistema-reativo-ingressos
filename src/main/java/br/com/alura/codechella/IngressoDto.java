package br.com.alura.codechella;

public record IngressoDto(Long id,
        Long eventoId,
        TipoIngresso tipo,
        Double valor,
        int total,
        int lote) {
    public static IngressoDto toDto(Ingresso ingresso) {
        return new IngressoDto(ingresso.getId(), ingresso.getEventoId(),
                ingresso.getTipo(), ingresso.getValor(), ingresso.getTotal(), ingresso.getLote());
    }

    public Ingresso toEntity() {
        Ingresso ingresso = new Ingresso();
        ingresso.setId(this.id);
        ingresso.setEventoId(this.eventoId);
        ingresso.setTipo(this.tipo);
        ingresso.setValor(this.valor);
        ingresso.setTotal(this.total);
        ingresso.setLote(this.lote);
        return ingresso;
    }
}
