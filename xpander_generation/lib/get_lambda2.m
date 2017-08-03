function [ l2 ] = get_lambda2( adj )
    lambda = sort(abs(eig(adj)));
    l2 = lambda(length(lambda)-1);
end

