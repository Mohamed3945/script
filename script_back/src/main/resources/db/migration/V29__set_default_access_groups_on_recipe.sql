UPDATE recipe
SET access_display_groups = 'ALL'
WHERE access_display_groups IS NULL;

UPDATE recipe
SET access_modify_groups = 'ALL'
WHERE access_modify_groups IS NULL;

ALTER TABLE recipe
    ALTER COLUMN access_display_groups SET DEFAULT 'ALL',
    ALTER COLUMN access_modify_groups SET DEFAULT 'ALL';
