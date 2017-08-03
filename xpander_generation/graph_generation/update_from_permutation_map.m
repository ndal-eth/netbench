function [A, factors] = update_from_permutation_map(A,n,perm, factors, ~)
    for key=1:n
        A(key, perm(key)) = 1;
        A(perm(key),key) = 1;
    end
end