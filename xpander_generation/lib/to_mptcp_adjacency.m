function to_mptcp_adjacency( adj, path )
    N = size(adj,2);
    f = fopen(path, 'w');
    for src=1:N
        for dst=src+1:N
            if is_neighbor( adj, src, dst)
                fwrite(f, sprintf('%d %d\n',src-1,dst-1));
            end
        end

    end
    fclose(f);
end
