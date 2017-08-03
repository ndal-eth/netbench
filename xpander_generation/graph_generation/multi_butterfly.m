function [B ] = multi_butterfly(is_level,n,add_vec,type)
    if type == 1
        B = multi_butterfly_1(is_level,n,add_vec);
    elseif type == 2
       B = multi_butterfly_2(is_level,n,add_vec);
   elseif type == 3
       B = multi_butterfly_3(is_level,n,add_vec);
    elseif type == 4
       B = multi_butterfly_4(is_level,n,add_vec);
    elseif type == 5
        B = multi_butterfly_5(is_level,n,add_vec);
    end
    
end

function [ B ] = multi_butterfly_5(is_level, n, add_vec )
    % this is the same as method 4/2 but using random permutations
    [A, levels] = butterfly(is_level,n);
    B = A;
    nodes_in_level = 2^levels;
    
    for level=0:levels
       cur_perm = randperm(nodes_in_level/2^level);
       for j=0:2^level-1
           for i=1:nodes_in_level/2^level
              node_ind = j*nodes_in_level/2^level+i;
              neigh_ind = j*nodes_in_level/2^level+cur_perm(i);%get_mod_result(i,add_vec(1),nodes_in_level/2^level);
              
              node = level*nodes_in_level+node_ind;
              neigh = level*nodes_in_level+neigh_ind;
              for ne=find(A(neigh,:))
                  if ne>node
                      B = add_multiple_links(B,node,ne);
                  end
              end
           end
       end
    end
end

function [ B ] = multi_butterfly_4(is_level, n, add_vec )
    % this is the same as method 2
    [A, levels] = butterfly(is_level,n);
    B = A;
    nodes_in_level = 2^levels;
    
    for level=0:levels
       for j=0:2^level-1
           for i=1:nodes_in_level/2^level
              node_ind = j*nodes_in_level/2^level+i;
              neigh_ind = j*nodes_in_level/2^level+get_mod_result(i,add_vec(1),nodes_in_level/2^level);
              
              node = level*nodes_in_level+node_ind;
              neigh = level*nodes_in_level+neigh_ind;
              for ne=find(A(neigh,:))
                  if ne>node
                      B = add_multiple_links(B,node,ne);
                  end
              end
           end
       end
    end
end

function [ B ] = multi_butterfly_3(is_level, n, add_vec )
    [A, levels] = butterfly(is_level,n);
    B = A;
    nodes_in_level = 2^levels;
    
    for add=add_vec
       for level=0:levels
          for ind=1:nodes_in_level
              node = level*nodes_in_level+ind;
              neigh_ind = get_mod_result(ind,add,nodes_in_level);
              disp([ind neigh_ind]);
              neigh = level*nodes_in_level+neigh_ind;
              
              for ne=find(A(neigh,:))
                  if ne>node
                      B = add_multiple_links(B,node,ne);
                  end
              end
          end
       end
    end
end

function [ B ] = multi_butterfly_2(is_level, n, add_vec )
    [A, levels] = butterfly(is_level,n);
    B = A;
    nodes_in_level = 2^levels;
    for add=add_vec
       for level=0:levels-1
           nodes_in_cur_level=2^(levels-level);
          for time=0:nodes_in_level/nodes_in_cur_level-1
              for ind=1:nodes_in_cur_level
%                   cntr = cntr+1;
                  node = level*nodes_in_level+time*nodes_in_cur_level+ind;
                  neigh_ind = get_mod_result(ind,add,nodes_in_cur_level);
    %               disp([ind neigh_ind]);
                  neigh = level*nodes_in_level+time*nodes_in_cur_level+neigh_ind;
                  for ne=find(A(neigh,:))
                      if ne>node
                          B = add_multiple_links(B,node,ne);
                      end
                  end
              end
          end
       end
    end
    
end

function [ B ] = multi_butterfly_1(is_level, n, add_vec )
    [A, levels] = butterfly(is_level,n);
    B = A;
    nodes_in_level = 2^levels;
    
    for add=add_vec
       for level=0:levels
          for ind=1:nodes_in_level
              node = level*nodes_in_level+ind;
              neigh_ind = get_mod_result(ind,add,nodes_in_level);
              disp([ind neigh_ind]);
              neigh = level*nodes_in_level+neigh_ind;
              
              temp = A(node,:) + A(neigh,:);
              B(node,:) = temp;
              B(:,node) = transpose(temp);
%               B(:,node) = transpose(temp);
%               B(:,neigh) = transpose(temp);
          end
       end
    end
end

function [val] = get_mod_result(start,add,n)
    val = start+add;
    if mod(val,n+1) == 0
        val = 1;
    else
        val = mod(val,n+1);
    end
end