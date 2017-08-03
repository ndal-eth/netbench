function adj = random_xpander_lift(d,lifts)
    n = (d+1)*lifts;
    adj = zeros([n n]);
    
    % go over first group allocate neighbors
    % go over second group allocate to which ever is left
    % ...

    for g1=1:d+1
        for g2=g1+1:d+1
            neighs = randperm(lifts);
            for i=1:lifts
                adj = add_link(adj, (g1-1)*lifts+i,(g2-1)*lifts+neighs(i));
            end
        end 
    end
    
    % If this is by any chance not ramanujan, try again :)
    if ~is_expander(adj,d)
        adj = random_xpander_lift(d,lifts);
    end
end