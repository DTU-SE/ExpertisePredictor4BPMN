-- set relative modeling time field
update metrics_evolution as t1 inner join (select model_id, max(modeling_time) as m from metrics_evolution group by model_id) as t2 on t1.model_id = t2.model_id
set t1.relative_modeling_time = t1.modeling_time / t2.m;

-- random sort
update metrics_evolution set rand = rand();
