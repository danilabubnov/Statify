create table albums
(
    spotify_id             varchar(50)  not null
        primary key,
    album_type             varchar(32)  not null
        constraint albums_album_type_check
            check ((album_type)::text = ANY
                   ((ARRAY ['ALBUM'::character varying, 'SINGLE'::character varying, 'COMPILATION'::character varying])::text[])),
    label                  varchar(255),
    name                   varchar(255) not null,
    popularity             integer,
    release_date_precision varchar(10)
        constraint albums_release_date_precision_check
            check ((release_date_precision)::text = ANY
                   ((ARRAY ['YEAR'::character varying, 'MONTH'::character varying, 'DAY'::character varying])::text[])),
    release_date_raw       varchar(10),
    release_day            integer,
    release_month          integer,
    release_year           integer,
    total_tracks           integer      not null
);

alter table albums
    owner to postgres;

create index idx_albums_release_year
    on albums (release_year);

create index idx_albums_release_month
    on albums (release_month);

create index idx_albums_name
    on albums (name);

create table artists
(
    spotify_id      varchar(50)  not null
        primary key,
    followers_total integer,
    name            varchar(255) not null,
    popularity      integer
);

alter table artists
    owner to postgres;

create index idx_artists_name
    on artists (name);

create table tracks
(
    spotify_id   varchar(50)  not null
        primary key,
    duration_ms  integer      not null,
    explicit     boolean      not null,
    name         varchar(255) not null,
    popularity   integer,
    track_number integer      not null,
    album_id     varchar(50)  not null
        constraint fkdcmijveo7n1lql01vav1u2jd2
            references albums
);

alter table tracks
    owner to postgres;

create index idx_tracks_name
    on tracks (name);

create table album_artists
(
    album_id  varchar(50) not null
        constraint fktblxj61x9u2a4qwpbd22bjxxg
            references albums,
    artist_id varchar(50) not null
        constraint fkpmmqobo7dghsklv6ocdqds4mb
            references artists,
    primary key (album_id, artist_id)
);

alter table album_artists
    owner to postgres;

create table album_images
(
    album_id     varchar(50) not null
        constraint fkbdn548tq89gkcmd9y68syvs99
            references albums,
    image_height integer,
    image_url    varchar(255),
    image_width  integer,
    image_order  integer     not null,
    primary key (album_id, image_order)
);

alter table album_images
    owner to postgres;

create table artist_images
(
    artist_id    varchar(50) not null
        constraint fk7cqpk25202oorwucah01lxn1k
            references artists,
    image_height integer,
    image_url    varchar(255),
    image_width  integer,
    image_order  integer     not null,
    primary key (artist_id, image_order)
);

alter table artist_images
    owner to postgres;

create table artist_genres
(
    artist_id varchar(50)  not null
        constraint fkrsiygs82pwn6xqk6prce2eana
            references artists,
    genre     varchar(100) not null,
    constraint uk_artist_genre
        unique (artist_id, genre)
);

alter table artist_genres
    owner to postgres;

create table track_artists
(
    track_id  varchar(50) not null
        constraint fkony1th5c1wj2jev5dj7iu3w0s
            references tracks,
    artist_id varchar(50) not null
        constraint fksl7o5dd8puim5tvojt2elt5cy
            references artists,
    primary key (track_id, artist_id)
);

alter table track_artists
    owner to postgres;

create table user_favorite_albums
(
    user_id  uuid                        not null,
    added_at timestamp(6) with time zone not null,
    album_id varchar(255)                not null
        constraint fk5vu509h44vcnujocgq6t598t1
            references albums,
    primary key (album_id, user_id)
);

alter table user_favorite_albums
    owner to postgres;

create index idx_user_favorite_albums_user
    on user_favorite_albums (user_id);

create index idx_user_favorite_albums_album
    on user_favorite_albums (album_id);

create index idx_user_favorite_albums_user_added_album
    on user_favorite_albums (user_id, added_at, album_id);

create table user_favorite_tracks
(
    user_id  uuid                        not null,
    added_at timestamp(6) with time zone not null,
    track_id varchar(255)                not null
        constraint fkkp43fbpd4nnkboonnc5fkuyy
            references tracks,
    primary key (track_id, user_id)
);

alter table user_favorite_tracks
    owner to postgres;

create index idx_user_favorite_tracks_user
    on user_favorite_tracks (user_id);

create index idx_user_favorite_tracks_track
    on user_favorite_tracks (track_id);

create index idx_user_favorite_tracks_user_added_track
    on user_favorite_tracks (user_id, added_at, track_id);

create table user_followed_artists
(
    user_id   uuid         not null,
    artist_id varchar(255) not null
        constraint fk457p23bnt0mb6xcc0ypuy7klx
            references artists,
    primary key (artist_id, user_id)
);

alter table user_followed_artists
    owner to postgres;

create index idx_user_followed_artists_user
    on user_followed_artists (user_id);

create index idx_user_followed_artists_artist
    on user_followed_artists (artist_id);

create index idx_user_followed_artists_user_artist
    on user_followed_artists (user_id, artist_id);