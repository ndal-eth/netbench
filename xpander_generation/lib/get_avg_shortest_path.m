function [ avg_path ] = get_avg_shortest_path( A,tors )
    sp_A = sparse(A);
    longest = graphallshortestpaths(sp_A,'directed',false);
    avg = 0;
    cntr = 0;
    for src=1:length(tors)
        for dst=src+1:length(tors)
            row = tors(src);
            col = tors(dst);

            avg = avg + longest(row,col);
            cntr = cntr+1;
        end
    end

    avg_path = avg/cntr;
end
