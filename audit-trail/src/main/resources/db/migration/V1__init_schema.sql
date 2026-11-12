create table audit_event
(
    id bigint generated always as identity primary key,
    log_time timestamp not null,
    actor varchar(100) not null,
    action varchar(100) not null,
    target varchar(100) not null
)