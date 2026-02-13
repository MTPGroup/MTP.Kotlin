create extension if not exists hstore;

drop table public.langchain_embeddings;
create table if not exists public.langchain_embeddings
(
    id        uuid default gen_random_uuid() primary key,
    content   text,
    metadata  jsonb,
    embedding extensions.vector(1024)
);

create index if not exists idx_langchain_embeddings_vector
    on public.langchain_embeddings
        using hnsw (embedding extensions.vector_cosine_ops)
    with (m = 16, ef_construction = 64);

-- 创建 metadata 上的 GIN 索引（用于过滤查询）
create index if not exists idx_langchain_embeddings_metadata
    on public.langchain_embeddings
        using gin (metadata);

-- 在 metadata.knowledgeBaseId 上创建 B-tree 索引（用于精确匹配过滤）
create index if not exists idx_langchain_embeddings_kb_id
    on public.langchain_embeddings
        using btree ((metadata ->> 'knowledgeBaseId'));
