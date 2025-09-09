SELECT cron.unschedule('recheck_in_progress_albums');

SELECT cron.schedule(
               'recheck_in_progress_albums',
               '*/1 * * * *',
               $$
    UPDATE albums
    SET mb_release_group_status = 'PENDING'
    WHERE mb_release_group_status = 'IN_PROGRESS'
      AND processing_started_at IS NOT NULL
      AND now() - processing_started_at > interval '90 minutes';
$$
);