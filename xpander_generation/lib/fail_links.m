function adj2 = fail_links( adj, perc )
    adj2 = adj;
    N = size(adj,2);
    for src=1:N
        for dst=src+1:N
            if is_neighbor(adj, src, dst) && rand < perc
               adj2 = remove_link(adj2, src, dst); 
            end
        end
    end
end

