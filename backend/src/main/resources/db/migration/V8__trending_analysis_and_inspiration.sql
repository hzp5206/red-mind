ALTER TABLE inspirations
    MODIFY COLUMN history_id BIGINT NULL;

ALTER TABLE inspirations
    ADD COLUMN trending_item_id BIGINT NULL AFTER history_id;

ALTER TABLE inspirations
    ADD INDEX idx_inspirations_trending_item_id (trending_item_id);

ALTER TABLE trending_copy_items
    ADD COLUMN analysis_json JSON NULL AFTER snapshot_json;
