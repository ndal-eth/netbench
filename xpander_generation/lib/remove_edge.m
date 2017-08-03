function A = remove_edge(A,v,w)
    A(v,w) = 0;
    A(w,v) = 0;
end

