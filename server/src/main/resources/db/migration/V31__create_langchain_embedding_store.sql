-- 为 LangChain4j 创建新的向量存储表
-- 保留旧的 knowledge_documents 表用于兼容

create table if not exists public.langchain_embeddings (
    embedding_id uuid primary key default gen_random_uuid(),
    embedding extensions.vector(1024) not null,
    text text not null,
    metadata jsonb not null default '{}'::jsonb
);

-- 创建向量索引（HNSW 索引，支持高性能近似搜索）
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
    using btree ((metadata->>'knowledgeBaseId'));

-- 创建搜索函数（兼容 LangChain4j 的搜索方式）
create or replace function public.match_langchain_embeddings(
    query_embedding extensions.vector(1024),
    knowledge_base_ids uuid[],
    match_threshold float default 0.5,
    match_count int default 10
)
returns table (
    embedding_id uuid,
    text text,
    metadata jsonb,
    similarity float
)
language plpgsql
set search_path = public, extensions
as $$
begin
    return query
    select
        le.embedding_id,
        le.text,
        le.metadata,
        1 - (le.embedding <=> query_embedding) as similarity
    from public.langchain_embeddings le
    where
        le.metadata->>'knowledgeBaseId' = any(knowledge_base_ids::text[])
        and 1 - (le.embedding <=> query_embedding) > match_threshold
    order by le.embedding <=> query_embedding
    limit match_count;
end;
$$;
