function [lambda1,lambda2] = get_spectral_gap_eigs(adj,n,d)
    lambda = eigs((1/d)*adj);
    lambda2 = abs(lambda(2)); %max( abs(lambda(1)),abs(lambda(n-1)) );
    lambda1 = lambda(1);
end
