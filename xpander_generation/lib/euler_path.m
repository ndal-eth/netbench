function path = euler_path(A,n,d)
	num_edges = n*d/2;
    path = zeros(num_edges, 1);
    copy_matrix = A;
    v = 1;
    step = 1;
    path(step) = v;
    while num_edges
        step = step+1;
        neighbors = find(copy_matrix(v,:));
        if mod(length(neighbors),2) == 0 || length(neighbors) ==1
            w = neighbors(1);
        else
            for w=neighbors
                copy_matrix(v,w)=0;
                copy_matrix(w,v)=0;
                if has_path(copy_matrix,v,w,n)
                    break
                else
                    copy_matrix(v,w)=1;
                    copy_matrix(w,v)=1;
                end
            end
        end
        copy_matrix(v,w)=0;
        copy_matrix(w,v)=0;
        path(step) = w;
        num_edges = num_edges-1;
        v = w;
    end
    clear copy_matrix;
end