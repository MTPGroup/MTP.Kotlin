alter table knowledge_files
    add column if not exists retry_count integer default 0;

update knowledge_files
set retry_count = 0
where retry_count is null;
