function [ bound ] = get_diameter_bound( A,d )
    n = size(A,2);
    bound = logb(n,d);
end

