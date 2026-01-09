create table ingressos(
    id bigint auto_increment not null,
    evento_id bigint not null,
    tipo varchar(30) not null,
    valor decimal(10, 2) not null,
    total int not null,

    primary key(id)
);