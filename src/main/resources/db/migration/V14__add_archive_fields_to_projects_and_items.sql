ALTER TABLE projects
    ADD COLUMN archived_at datetime NULL;

ALTER TABLE project_items
    ADD COLUMN archived_at datetime NULL;

CREATE INDEX idx_projects_archived_status ON projects (archived_at, status);

CREATE INDEX idx_project_items_project_archived ON project_items (project_id, archived_at);
