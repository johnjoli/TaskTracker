create table tasks (
    id bigserial primary key,
    title varchar(120) not null,
    description varchar(1000),
    status varchar(20) not null,
    priority varchar(20) not null,
    due_date timestamp,
    created_at timestamp not null,
    updated_at timestamp not null
);
