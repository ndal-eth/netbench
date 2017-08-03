function [ res,l,rama ] = is_expander( mat, d )
    rama = 2*sqrt(d-1);
    lambda = sort(abs(eig(mat)));
    res = lambda(length(lambda)-1)<rama;
    l = lambda(length(lambda)-1);
end

