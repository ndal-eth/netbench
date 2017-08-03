function res = get_laplacian_results(A,d)
    n = size(A,1);
    res = zeros([1 n*d/2]);
    i = 1;
    for row=1:n
        for col=row+1:n
            if A(row,col) == 1 
                lambda = eig(get_laplacian(remove_edge(A,row,col)));
                res(i) = lambda(2);
                i = i+1;
            end
        end
    end
end
