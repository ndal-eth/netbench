function lap = get_laplacian(adj_sp)
    adj = full(adj_sp);
    lap = diag(sum(adj~=0,2)) - adj;
end

