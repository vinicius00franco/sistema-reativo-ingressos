create table vendas(
    id bigint auto_increment not null,
    ingresso_id bigint not null,
    total int not null,

    primary key(id)
);