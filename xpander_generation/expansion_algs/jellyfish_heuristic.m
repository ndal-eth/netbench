function [ A ] = jellyfish_heuristic( A,n,d )
    all_edges = find(upper(A));
    remove_edges = randsample(all_edges,d/2);
    for i=remove_edges
        v = int32(mod(i,n+1));
        u = int32(i/(n+1));
        A(v,u) = 0 ; A(u,v) = 0;
        A(n+1,v)=1 ; A(v, n+1) = 1;
        A(n+1,u)=1 ; A(u,n+1) = 1;
    end
end

