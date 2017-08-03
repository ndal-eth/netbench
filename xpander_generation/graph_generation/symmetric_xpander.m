function [min_adj,cntr] = symmetric_xpander(d,lifts, max_cntr)
    assert( mod(d,2) == 0 );
    min_val = 1.1;
    min_adj = get_xpander(d,lifts);
    cntr = 0;
    [res, lam, rama] = is_expander(min_adj,d);
    % If this is by any chance not ramanujan, try again :)
    while ~res && cntr<max_cntr
        
        cntr = cntr +1;
        adj = get_xpander(d,lifts);
        [res, lam, rama] = is_expander(adj,d);
%         if mod(cntr,100) == 0
        disp(cntr);
        disp([lam lam/rama min_val]);
        if lam/rama < min_val
            min_adj = adj;
            min_val = lam/rama;
        end
        clearvars adj;
        disp('------');
%         end
    end
end

function adj = get_xpander(d, lifts)
    n = (d+1)*lifts;
    adj = zeros([n n]);
    
    % go over first group allocate neighbors
    % go over second group allocate to which ever is left
    % ...
    
    for g1=1:d+1
        for g2=g1+1:2:d
        
%         for g2=g1+1:d+1
            neighs = randperm(lifts);
            % neighs = randperm(lifts);
            tmp1 = g2;
            tmp2 = g2+1;
            for i=1:lifts
                adj = add_link(adj, (g1-1)*lifts+i,(tmp1-1)*lifts+neighs(i));
                adj = add_link(adj, (g1-1)*lifts+i,(tmp2-1)*lifts+neighs(i));
%                 adj = add_link(adj, (g1-1)*lifts+i,(g2-1)*lifts+neighs(i));
            end
        end
    end
end
