function [ deg_count ] = get_deg_count(A)
    deg_arr = get_deg_arr(A);
    unv = unique(deg_arr);
    deg_count = histc(deg_arr, unv);
end