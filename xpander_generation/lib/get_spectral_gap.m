function [lambda1,lambda2] = get_spectral_gap(adj,n,d)
    if nargin == 1
        n = size(adj,2);
    end
    
%      lambda = eig(adj);
%      lambda2 = max( abs(lambda(1)),abs(lambda(n-1)) );
%      lambda1 = lambda(n);
    lambda = sort(abs(eig(adj)));
    lambda1 = lambda(n);
    lambda2 = lambda(n-1);
end