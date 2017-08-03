function [B,lambda1,lambda2, list,factors] = xpander_heuristic_lap_eigs(adj,n, d, list,factors)
  [B, lambda1, lambda2, list] = xpander_heuristic(adj,n,d,list,@getLaplacianGapEigs);
end

function gap = getLaplacianGapEigs(adj)
    tic
    if size(adj,1)>200
        lap = diag(sum(adj~=0,2)) - adj;
        lambda = eigs(lap,6,'sm');
    else
       adj = full(adj);
       lap = diag(sum(adj~=0,2)) - adj;
       lambda = eig(lap); 
    end
    gap = lambda(2);
    toc
end
