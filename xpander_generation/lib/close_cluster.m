function close_cluster()
poolobj = gcp('nocreate');
delete(poolobj);
end

