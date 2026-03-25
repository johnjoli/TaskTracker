create table users (
    id bigserial primary key,
    username varchar(50) not null unique,
    password varchar(255) not null,
    role varchar(20) not null,
    created_at timestamp not null
);

alter table tasks
    add column created_by_id bigint;

alter table tasks
    add column assignee_id bigint;

alter table tasks
    add constraint fk_tasks_created_by
        foreign key (created_by_id) references users (id);

alter table tasks
    add constraint fk_tasks_assignee
        foreign key (assignee_id) references users (id);
