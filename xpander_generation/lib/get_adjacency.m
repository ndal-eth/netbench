function A = get_adjacency(n)
    A = ones(n)-eye(n);
    A = sparse(A);
end