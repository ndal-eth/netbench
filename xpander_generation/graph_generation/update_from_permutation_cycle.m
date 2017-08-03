function [A, factors] = update_from_permutation_cycle(A,n,perm, ....
                                        factors, counter)
    for key=1:n-1
        A(perm(key), perm(key+1)) = 1;
        A(perm(key+1),perm(key)) = 1;
    end
    A(perm(1), perm(n)) = 1;
    A(perm(n),perm(1)) = 1;
    factor = containers.Map('KeyType','int32','ValueType','any');
    factor(1) = perm;
    factors(counter) = factor;
end