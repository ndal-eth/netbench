function [A, factors] = permutations_graph(n, d, update_from_perm, factors_from_perm)
    A= zeros([n n]); 
    counter = 0;
    factors = containers.Map('KeyType','int32','ValueType', 'any');
    tic
    while counter ~= d/2
        flag = 1;
        perm = randperm(n);
        % Check if we have collisions
        for key=1:n
            if      perm(key) == key || ...
                    A(key,perm(key)) == 1 || ...
                    key == perm( perm(key) )
                flag = 0;
                break;
            end
        end
        if flag == 1
%             for key=1:n
%                 A(key, perm(key)) = 1;
%                 A(perm(key),key) = 1;
%             end
            [A, factors] = update_from_perm(A,d,perm,factors,counter);
            counter  = counter + 1 ;
        end
    end
    toc
    while ~validate(A,n,d)
       disp('Invalid permutation graph, regenerating');
       A = get_permutations_graph(n,d);
    end

    if ~factors_from_perm
        factors = get_2_factors(A,n,d);
    end
end



function res = validate(A,n,d)
    res = sum(sum(A,2))==n*d;
end
