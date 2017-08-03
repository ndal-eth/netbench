function [B,lambda1,lambda2, list, factors] = xpander_irregular_heuristic_lap(adj,n, d, list,factors)
  [B, lambda1, lambda2, list] = xpander_irregular_heuristic(adj,n,d,list,@getAdjacencyGapLap);
end

function gap = getAdjacencyGapLap(adj_sp)
    adj = full(adj_sp);
    lap = diag(sum(adj~=0,2)) - adj;
    lambda = eig(lap);

    gap = lambda(find(lambda,1) );
end
