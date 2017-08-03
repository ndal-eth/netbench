function print_not_connected( A )
    n= size(A,2);
    for i = 1:n
        for j=i+1:n
            if ~has_path(A,i,j)
                disp([ i j] );
            end
        end
    end
end

