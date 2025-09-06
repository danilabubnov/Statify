create table users
(
    id         uuid         not null
        primary key,
    created_at timestamp(6) with time zone,
    email      varchar(255) not null
        constraint uk6dotkott2kjsp8vw4d0m25fb7
            unique,
    password   varchar(255),
    updated_at timestamp(6) with time zone
);

alter table users
    owner to postgres;

create index idx_user_email
    on users (email);

create table user_authorities
(
    user_id     uuid not null
        constraint fkhiiib540jf74gksgb87oofni
            references users,
    authorities varchar(255)
);

alter table user_authorities
    owner to postgres;

create table spotify_info
(
    spotify_id    varchar(255) not null
        primary key,
    created_at    timestamp(6) with time zone,
    email         varchar(255),
    refresh_token varchar(1000),
    updated_at    timestamp(6) with time zone,
    user_id       uuid
        constraint ukpnshhbafs7rbarnmli421htl6
            unique
        constraint fkmv9k5tdsy8em97maj238k91wb
            references users
);

alter table spotify_info
    owner to postgres;

create table oauth2_link_state
(
    id                    uuid not null
        primary key,
    authorization_request text,
    created_at            timestamp(6) with time zone,
    user_id               uuid
        constraint fkcemwglvddjkt5srt3kjtdgx63
            references users
);

alter table oauth2_link_state
    owner to postgres;

create table user_spotify_library
(
    id                   uuid         not null
        primary key,
    created_at           timestamp(6) with time zone,
    last_synchronized_at timestamp(6) with time zone,
    status               varchar(255) not null
        constraint user_spotify_library_status_check
            check ((status)::text = ANY
                   ((ARRAY ['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'PARTIALLY_SYNCED'::character varying])::text[])),
    updated_at           timestamp(6) with time zone,
    user_id              uuid         not null
        constraint fknmd1qimevr0hkvuyl3jfsau0u
            references users
);

alter table user_spotify_library
    owner to postgres;

create index idx_user_id
    on user_spotify_library (user_id);

create index idx_status
    on user_spotify_library (status);