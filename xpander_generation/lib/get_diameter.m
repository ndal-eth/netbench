function [diam] = get_diameter(A)
    sp_A = sparse(A);
    longest = graphallshortestpaths(sp_A,'directed',false);
    diam = max(longest(:));
end