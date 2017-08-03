function [B,lambda1,lambda2, list,factors] = xpander_heuristic_lap(adj,n, d, list,factors)
   disp(n);
  [B, lambda1, lambda2, list] = xpander_heuristic(adj,n,d,list,@getLaplacianGap);
  disp('-------');
end

function gap = getLaplacianGap(adj_sp)
    adj = full(adj_sp);
    lap = diag(sum(adj~=0,2)) - adj;
    lambda = eig(lap);

    gap = lambda(2);
end
