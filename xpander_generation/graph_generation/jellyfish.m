%% This creates a random graph ala Jellyfish with n nodes and degree d
function [ A ] = jellyfish( n,d )
    A = zeros( [ n n] );
    full = zeros( [ n 1] );
    if d+1 == n
        A = get_adjacency(n);
    else
       nodes_to_link = 1:n;
       more_perms = true;
       while more_perms
           more_perms = false;
           permutation = nodes_to_link(randperm(length(nodes_to_link)));
           for i=1:length(nodes_to_link)
              if i > length(nodes_to_link)
                  continue;
              end
              node = nodes_to_link(i);
              neigh = permutation(i);
              if full(node)==d || full(neigh)==d || node == neigh
                  continue;
              elseif A(node,neigh) == 0
                 more_perms = true;
                 A = add_link(A,node,neigh);
                 full(node) = full(node)+1;
                 full(neigh) = full(neigh)+1;
                 if full(node) == d
                    nodes_to_link = nodes_to_link(nodes_to_link~=node);
                 end
                 if full(neigh) == d
                    nodes_to_link = nodes_to_link(nodes_to_link~=neigh);
                 end
              end
           end
       end
       if ~isempty(nodes_to_link)
           A = jellyfish(n,d);
       elseif sum(sum(A,2)) ~= n*d
           A = jellyfish(n,d);
       end

    end
end
