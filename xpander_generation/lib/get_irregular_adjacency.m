function A = get_irregular_adjacency(n)
    A = ones(n)-eye(n);
    A = remove_triangle(A,n);
    A = sparse(A);
end

function A = remove_triangle(A,n)
    
    % remove edge=(n,n-1)
    A(n-1,n) = 0;
    A(n, n-1) = 0;
    
    % remove edge=(n-2,n-1)
    A(n-1,n-2) = 0;
    A(n-2, n-1) = 0;
    
    % remove edge=(n,n-2)
    A(n-2,n) = 0;
    A(n, n-2) = 0;
end