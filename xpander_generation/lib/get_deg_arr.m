function [deg_arr] = get_deg_arr(A)
    n = size(A,1);
   
    deg_sum = sum(A,2);
    deg_arr = zeros([n 1]);
    for i=1:n
        deg_arr(i) = deg_sum(i);
    end
end

