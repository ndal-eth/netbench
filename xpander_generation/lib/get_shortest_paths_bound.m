function [ res ] = get_shortest_paths_bound( N, d )
    [k,R] = get_k(N,d);

    res = k * R;
    func = @(a) a*d*((d-1)^(a-1));

    res = res + sum( arrayfun( func , 1:k-1) );
    res = res/(N-1);
end

function [k, R] = get_k(N,d)
    k = 1;
    rTag = 0;
    func = @(a) d*((d-1)^(a-1));
    
    while rTag >= 0
       k = k+1;
       R = rTag;
       tmp = sum( arrayfun( func , 1:k-1) );
       rTag = (N-1) - tmp;
    end
    k=k-1;
end
% end
% def find_k(N,d):
%     k=1
%     res1 = 0
%     while res1 >= 0:
%         k+=1
%         res=res1
%         res1 = N-1 - sum([d*(d-1)**(j-1) for j in range(1,k)])
%         #print res1, k
% 
%     return k-1,res
% 
% def get_avg_path_len(N,d):
%     k,R = find_k(N,d)
%     
