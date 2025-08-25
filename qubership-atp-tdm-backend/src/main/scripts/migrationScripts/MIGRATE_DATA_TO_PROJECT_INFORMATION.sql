DO
$$
begin
        if exists
            (select 1 from information_schema.tables where table_name = 'project_information')
        then
            insert into project_information (project_id, time_zone, date_format, time_format)
            select distinct tc.project_id, 'GMT+03:00', 'd MMM yyyy', 'hh:mm:ss a'
            FROM test_data_table_catalog tc
            WHERE NOT EXISTS (SELECT 1 FROM project_information pi WHERE pi.project_id = tc.project_id);
end if;
end
$$;
