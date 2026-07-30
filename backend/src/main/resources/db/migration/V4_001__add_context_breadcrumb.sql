-- Context breadcrumb: where the user left off on a task
ALTER TABLE task ADD COLUMN last_context TEXT DEFAULT NULL;
