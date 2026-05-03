create table task_comments (
    id bigserial primary key,
    text varchar(1000) not null,
    task_id bigint not null,
    author_id bigint not null,
    created_at timestamp not null,
    constraint fk_task_comments_task
        foreign key (task_id) references tasks(id) on delete cascade,
    constraint fk_task_comments_author
        foreign key (author_id) references users(id)
);
