ALTER TABLE operation_logs
ADD COLUMN snapshot_before TEXT NULL AFTER detail_text,
ADD COLUMN snapshot_after TEXT NULL AFTER snapshot_before;
