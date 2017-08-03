function [adj,lambda1,lambda2, list, factors] = from_2_factors(adj,n, d, list, factors)
    [adj,lambda1,lambda2, list, factors] = get_iteration_res(1,adj,n, d, list, factors);
end

function [adj,lambda1,lambda2, list, factors] = get_iteration_res(iter,adj,n, d, list, factors)
    lambda2_min = flintmax;
    counter = 0;
    while counter < iter
        try
            [adj_temp,lambda1_temp,lambda2_temp, list_temp, factors_temp] = get_temp_results(adj,n,d,list,factors);
            if lambda2_temp < lambda2_min
               adj_min = adj_temp;
               lambda1_min = lambda1_temp;
               lambda2_min = lambda2_temp;
               list_min = list_temp;
               factors_min = factors_temp;
            end
            counter = counter + 1;
        catch ME
            counter = counter + 0;
        end
    end
    adj = adj_min;
    lambda1 = lambda1_min;
    lambda2 = lambda2_min;
    list = list_min;
    factors = factors_min;
end

function cpy_factors = copy_factors(factors, d)
    cpy_factors = containers.Map('KeyType','int32','ValueType', 'any');
    for i=1:int32(d/2)
        factor = containers.Map('KeyType','int32','ValueType','any');
        factor_org = factors(i);
        for j=1:factor_org.Count
            factor(j) = factor_org(j);
        end
        cpy_factors(i) = factor;
    end
end

function [adj, neighs, circle, factor, cpy_factors, placed ] = place_edge_in_circle(adj, n, factor, i, circle, circle_ind, v_ind, neighs, cpy_factors)
    placed = 0;
    len_circ = length(circle);
    v = circle(v_ind);
    if v_ind+1 == len_circ
        u_ind = len_circ;
    else
        u_ind = mod(v_ind+1,len_circ);
    end
    u = circle(u_ind);
    if neighs(u) == 0 && neighs(v) == 0
        neighs(u) = 1; neighs(v) = 1;
        adj(u,v) = 0 ; adj(v,u) = 0;
        adj(n+1,v) = 1; adj(v,n+1) = 1;
        adj(n+1,u) = 1; adj(u,n+1) = 1;
        circle = [circle(1:v_ind) n+1 circle(v_ind+1:end)];
        factor(circle_ind) = circle;
        cpy_factors(i) = factor;
        placed = 1;
    end
end

function [adj,lambda1,lambda2, list, cpy_factors] = get_temp_results(adj,n, d, list, factors)
    neighs = zeros([1 n]);
    cpy_factors = copy_factors(factors,d );
    for i=1:int32(d/2)
        factor = cpy_factors(i);
        placed = 0;
        placed_ind = [ ];
        
        while ~placed
            if factor.Count > 1
                res = setdiff(1:factor.Count,placed_ind);
                circle_ind = randsample(res,1 );
                placed_ind = [placed_ind circle_ind];
            else
                circle_ind = 1;
            end
%             if circle_ind > 0
                circle = factor(circle_ind);

                len_circ = length(circle);

                reduced_circle = zeros([ 1 len_circ]);
                for v_ind=1:length(circle)
                    v = circle(v_ind);
                    if v_ind+1 == len_circ
                        u_ind = len_circ;
                    else
                        u_ind = mod(v_ind+1,len_circ);
                    end

                    u = circle(u_ind);

                    if neighs(u) == 0 && neighs(v) == 0
                        reduced_circle(v_ind) = 1;
                    end
                end
                % Select edge from available edges only
                indices = find(reduced_circle);
                if size(indices,2) > 0
                   v_ind = indices(randi(size(indices,2)));
                    [adj, neighs, circle, factor, cpy_factors, placed ] = place_edge_in_circle(adj, n, factor, i, circle, circle_ind, v_ind, neighs, cpy_factors);
                elseif factor.Count == 1
                   exception = MException('BadFactorPlacement','No room left in an euler path');
                   throw(exception);
                end
        end
    end

    [lambda1, lambda2] = get_spectral_gap(adj,n+1,d);
end