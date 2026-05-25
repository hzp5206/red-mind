ALTER TABLE generation_histories
    ADD COLUMN final_title VARCHAR(255) NULL AFTER results,
    ADD COLUMN final_result JSON NULL AFTER final_title,
    ADD COLUMN optimization_actions JSON NULL AFTER final_result,
    ADD COLUMN final_score DECIMAL(4,2) NULL AFTER optimization_actions,
    ADD COLUMN last_modified_at DATETIME NULL AFTER final_score;
