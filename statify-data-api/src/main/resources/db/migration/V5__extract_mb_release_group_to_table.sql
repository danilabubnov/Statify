create table album_release_groups
(
    spotify_id        varchar(50) primary key
        references albums (spotify_id) on delete cascade,
    mb_release_group        varchar,
    mb_release_group_status varchar(32) not null default 'PENDING'
        constraint album_release_groups_status_check
            check ((mb_release_group_status)::text = ANY
                   (ARRAY ['PENDING', 'IN_PROGRESS', 'FOUND', 'BARCODE_NOT_FOUND', 'NAME_NOT_FOUND'])),
    processing_started_at   timestamp(6) with time zone
);

insert into album_release_groups (spotify_id, mb_release_group, mb_release_group_status, processing_started_at)
select spotify_id,
       mb_release_group,
       case mb_release_group_status
           when 'NOT_FOUND' then 'BARCODE_NOT_FOUND'
           else mb_release_group_status
           end,
        processing_started_at
from albums
where mb_release_group is not null
    or processing_started_at is not null;

alter table albums
    drop column mb_release_group,
    drop column mb_release_group_status,
    drop column processing_started_at;


DROP INDEX IF EXISTS idx_albums_mb_release_group_status_spotify_id;
DROP INDEX IF EXISTS idx_albums_mb_release_group_status_processing_started_at;