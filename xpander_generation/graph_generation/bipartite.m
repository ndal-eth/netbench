%% This creates the complete bipartite graph
function [ A ] = bipartite( n, m )
    A = zeros([ n+m n+m]);
    for i=1:n
       for j =1:m
           A = add_link(A,i,n+j);
       end
    end
end
