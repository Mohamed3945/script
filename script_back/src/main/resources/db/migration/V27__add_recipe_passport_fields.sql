ALTER TABLE recipe
    ADD COLUMN wafer VARCHAR(32) NOT NULL DEFAULT 'PRESENT',
    ADD COLUMN iapc VARCHAR(32) NOT NULL DEFAULT 'NO',
    ADD COLUMN resumable VARCHAR(32) NOT NULL DEFAULT 'NO',
    ADD COLUMN chamber_type VARCHAR(255) NULL,
    ADD COLUMN access_display_groups VARCHAR(255) NULL,
    ADD COLUMN access_modify_groups VARCHAR(255) NULL,
    ADD COLUMN uda_file VARCHAR(255) NULL,
    ADD COLUMN type VARCHAR(64) NULL,
    ADD COLUMN max_time INT NULL,
    ADD COLUMN template VARCHAR(255) NULL;

ALTER TABLE recipe
    ADD CONSTRAINT chk_recipe_wafer_mode
    CHECK (wafer IN ('PRESENT', 'ABSENT', 'DONT_CARE')),
    ADD CONSTRAINT chk_recipe_iapc_mode
    CHECK (iapc IN ('NO', 'YES', 'REQUIRED', 'REQUIRED_IF_NO_HOST')),
    ADD CONSTRAINT chk_recipe_resumable_mode
    CHECK (resumable IN ('YES', 'NO')),
    ADD CONSTRAINT chk_recipe_max_time
    CHECK (max_time IS NULL OR max_time >= 0);
