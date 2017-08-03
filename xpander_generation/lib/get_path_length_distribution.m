function [ distro ] = get_path_length_distribution( A ,tors,max_size)
    sp_A = sparse(A);
    
    longest = graphallshortestpaths(sp_A,'directed',false);

    diam = get_diameter(A);
    if max_size < diam
        max_size = diam;
    end
    distro = zeros([ max_size 1]);
    cntr = 0;
    for src=1:length(tors)
        for dst=src+1:length(tors)
            row = tors(src);
            col = tors(dst);
            path_len = longest(row,col);
            distro(path_len) = distro(path_len)+1;
            cntr = cntr+1;
        end
    end
    
    distro = distro/cntr*100;
    
end

