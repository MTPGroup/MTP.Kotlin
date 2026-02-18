alter table knowledge_files
    drop constraint if exists knowledge_files_status_check,
    add constraint knowledge_files_status_check
        check ( status in ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') );