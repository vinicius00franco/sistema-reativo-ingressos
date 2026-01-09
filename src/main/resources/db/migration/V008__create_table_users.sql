create table users(
    id bigint auto_increment not null,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    role varchar(30) not null,

    primary key(id)
);