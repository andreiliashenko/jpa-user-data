drop table if exists
    groups_to_users, groups_to_grants, user_groups, users, user_grants;

create table user_grants (
    grant_id bigint(20) unsigned not null,
    description varchar(200) default null,
    primary key (grant_id)
) engine=InnoDB default charset=utf8;

create table users (
    user_id bigint(20) unsigned not null,
    name varchar(100) default null,
    login varchar(50) default null,
    password varchar(200) default null,
    primary key (user_id),
    key login (login)
) engine=InnoDB default charset=utf8;

create table user_groups (
    group_id bigint(20) unsigned not null,
    name varchar(100) default null,
    parent_id bigint(20) unsigned default null,
    primary key (group_id),
    key group_to_parent_idx (parent_id),
    constraint group_to_parent foreign key (parent_id) references user_groups (group_id)
        on delete set null on update cascade
) engine=InnoDB default charset=utf8;

create table groups_to_grants (
    group_id bigint(20) unsigned not null,
    grant_id bigint(20) unsigned not null,
    key to_group_idx (group_id),
    key to_grant_idx (grant_id),
    constraint grant_to_group foreign key (group_id) references user_groups (group_id)
        on delete cascade on update cascade,
    constraint group_to_grant foreign key (grant_id) references user_grants (grant_id)
        on delete cascade on update cascade
) engine=InnoDB default charset=utf8;

create table groups_to_users (
    group_id bigint(20) unsigned not null,
    user_id bigint(20) unsigned not null,
    key to_group_idx (group_id),
    key to_user_idx (user_id),
    constraint user_to_group foreign key (group_id) references user_groups (group_id)
        on delete cascade on update cascade,
    constraint group_to_user foreign key (user_id) references users (user_id) 
        on delete cascade on update cascade
) engine=InnoDB default charset=utf8;

create table if not exists id_generation_sequences (
    entity_set varchar(20) not null,
    last_id bigint(20) unsigned default null,
    primary key (entity_set),
    unique key idid_generation_sequences_unique (entity_set)
) engine=innodb default charset=utf8;

insert into
    id_generation_sequences (entity_set, last_id)
values
    ('user-data', 2000000000001000000)
on duplicate key update
    last_id = 2000000000001000000;