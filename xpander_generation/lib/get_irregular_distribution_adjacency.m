function A = get_irregular_distribution_adjacency(n, degs_percent)
    sorted_degs = sortrows(degs_percent,2);
%     A = ones(n)-eye(n);
    A = zeros( [n n] );
    allocated_deg = 0;
    for i=1:size(sorted_degs,2)
        nodes = n*sorted_degs(i,2);
        deg = sorted_degs(i,1);
        for j=1:n
            
        end
        
    end
    %A = ones(n)-eye(n);
    %A = remove_triangle(A,n);
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