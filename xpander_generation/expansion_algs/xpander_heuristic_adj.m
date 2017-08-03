function [B,lambda1,lambda2, list, factors] = xpander_heuristic_adj(adj,n, d, list,factors)
  [B, lambda1, lambda2, list] = xpander_heuristic(adj,n,d,list,@getAdjacencyGapAdj);
end

function gap = getAdjacencyGapAdj(adj_sp)
    adj = full(adj_sp);
    lambda = sortrows(abs(eig(adj)),-1);
    %disp([lambda(1), lambda(size(adj,1)-1), lambda(size(adj,1))]);
%     lambda2 = max(abs(lambda(1)), abs(lambda(size(adj,1)-1)));
    %gap =  %lambda(find(lambda,1) );
    gap = lambda(1)-lambda(2);
end
