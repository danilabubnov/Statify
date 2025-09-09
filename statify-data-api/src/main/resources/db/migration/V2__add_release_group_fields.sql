alter table albums
    add mb_release_group varchar;

alter table albums
    add mb_release_group_status varchar(32) not null
        constraint albums_mb_release_group_status_check
            check (mb_release_group_status in (
                                         'PENDING',
                                         'IN_PROGRESS',
                                             'FOUND',
                                         'NOT_FOUND'
                )) default 'PENDING';

alter table albums
    add processing_started_at timestamp(6) with time zone;

CREATE EXTENSION IF NOT EXISTS pg_cron;

SELECT cron.schedule(
               'recheck_in_progress_albums',
               '*/1 * * * *',
$$
    UPDATE albums
    SET mb_release_group_status = 'PENDING'
    WHERE mb_release_group_status = 'IN_PROGRESS'
        AND processing_started_at IS NOT NULL
        AND now() - processing_started_at > interval '15 minutes';
$$
);