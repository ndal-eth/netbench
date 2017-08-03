function start_cluster(poolsize, start)
   if ~start
       return
   end
   myCluster = parcluster;
   myCluster.NumWorkers=poolsize;
   parpool(myCluster,poolsize);
end