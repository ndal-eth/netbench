function [lambda1,lambda2] = get_irregular_spectral_gap(adj,n)
    lambda = eig(adj);
    lambda2 = max( abs(lambda(1)),abs(lambda(n-1)) );
    lambda1 = lambda(n);
end