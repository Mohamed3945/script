create table parameter_group (
    id bigint not null auto_increment,
    name varchar(128) not null,
    step_type varchar(32) not null,
    order_index int not null,
    create_time datetime(6) not null,
    revise_time datetime(6) not null,
    primary key (id),
    constraint uq_parameter_group_step_type_name unique (step_type, name),
    constraint uq_parameter_group_step_type_order unique (step_type, order_index)
);

alter table parameter_definition
    add column parameter_group_id bigint null,
    add column order_index_in_group int not null default 0;

alter table parameter_definition
    add constraint fk_parameter_definition_group
    foreign key (parameter_group_id) references parameter_group(id);

-- 1) Normalize and deduplicate legacy group names first
create temporary table tmp_group_source as
select
    pd.step_type as step_type,
    trim(pd.parameter_group) as group_name,
    min(coalesce(pd.parameter_group_order, 0)) as legacy_group_order
from parameter_definition pd
where pd.parameter_group is not null
  and trim(pd.parameter_group) <> ''
group by
    pd.step_type,
    trim(pd.parameter_group);

-- 2) Compute a unique stable order per step_type
create temporary table tmp_parameter_groups as
select
    src.step_type,
    src.group_name,
    row_number() over (
        partition by src.step_type
        order by src.legacy_group_order, src.group_name
    ) - 1 as computed_order
from tmp_group_source src;

insert into parameter_group (name, step_type, order_index, create_time, revise_time)
select
    tmp.group_name,
    tmp.step_type,
    tmp.computed_order,
    now(6),
    now(6)
from tmp_parameter_groups tmp;

update parameter_definition pd
join parameter_group pg
  on pg.name = trim(pd.parameter_group)
 and pg.step_type = pd.step_type
set pd.parameter_group_id = pg.id
where pd.parameter_group is not null
  and trim(pd.parameter_group) <> '';

-- 3) Stable order inside each group
create temporary table tmp_definition_rank as
select
    pd.id,
    row_number() over (
        partition by pd.parameter_group_id
        order by pd.name, pd.id
    ) - 1 as computed_order
from parameter_definition pd
where pd.parameter_group_id is not null;

update parameter_definition pd
join tmp_definition_rank ranked on ranked.id = pd.id
set pd.order_index_in_group = ranked.computed_order;

drop temporary table if exists tmp_definition_rank;
drop temporary table if exists tmp_parameter_groups;
drop temporary table if exists tmp_group_source;

alter table parameter_definition
    add constraint uq_parameter_definition_group_order
    unique (parameter_group_id, order_index_in_group);