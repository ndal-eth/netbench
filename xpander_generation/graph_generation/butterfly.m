%% This creates a butterfly graph.
function [ A, levels ] = butterfly( n )

    levels = ceil(log2(n));
    % end
    A = get_butterfly(levels);
end

function [A] = get_butterfly(levels)
    [c{1:levels}] = ndgrid(logical([0 1]));
    c = cat(levels+1,c{levels:-1:1});
    c = reshape(c,[],levels);

    actual_layers = levels+1;

    nodes = 2^levels*actual_layers;
    A = zeros([nodes nodes]);
    for level=0:levels-1
        for node=1:2^levels
            node_vec = c(node,:);
            for neigh=1:2^levels
                neigh_vec = c(neigh,:);
                diff = find(node_vec~=neigh_vec);
                if isempty(diff)
                    A = add_link(A,(level+1)*2^levels+neigh,level*2^levels+node);
                elseif length(diff) == 1 && diff(1)==level+1
                    A = add_link(A,(level+1)*2^levels+neigh,level*2^levels+node);
                end
            end
        end
    end
end
% function [B] = add_level(A, cur_level, num_in_level)
%     num_in_cur = size(A,2);
%     nodes = 2*num_in_cur + 2*num_in_level;
%     B = zeros([nodes nodes]);
%     new_level_num = 2*num_in_level;
%
%     for level=2:cur_level+1
%         cur_in_level = new_level_num/level;
%         for time=1:level
%             for i=1:cur_in_level
%                 node = new_level_num*(level-1)+(time-1)*cur_in_level+i;
%                 if i> cur_in_level/2
%                     neigh = 0;
%                 else
%                     neigh = 0;
%                 end
%                 B = add_link(B,node,neigh);
%             end
%         end
%     end
%
%     % add stright links : B = add_link(B,i,level*new_level_num+i);
%
%     % connect new layer
%     for i=1:new_level_num
%         B = add_link(B,i,new_level_num+i);
%         if i> new_level_num/2
%           B = add_link(B,i,i+new_level_num/2);
%         else
%           B = add_link(B,i,i+new_level_num+new_level_num/2);
%         end
%     end
%
%     % connect existing layers
%     for l=1:cur_level
%         for i=1:num_in_level
%             neighs = find(A(:,num_in_level*(l-1)+i));
%             for n =neighs
%                 % first copy
%                 B = add_link(B,new_level_num*l+i,new_level_num*(l+1) + );
%
%                 % second copy
%                 B = add_link(B,new_level_num*l+i+num_in_level,1);
%             end
%         end
%     end
% %     for l=0:cur_level-1
% %         for i=1:num_in_level
% %            B = B.add_link(B, l*num_in_level+i, (l+1)*new_level_num+
% %         end
% %     end
% %
% %
% %     B(1:num_in_cur, 1:num_in_cur) = A;
% %     B(num_in_cur+1:2*num_in_cur, num_in_cur+1:2*num_in_cur) = A;
% %
% %
% %
% %     for i=1:new_level_num
% %        B = add_link(B, 2*num_in_cur+i, 2*new_level_num+i);
% %        B = add_link(B, 2*num_in_cur+i,
% %     end
% end
%
% function [A] = butterfly_1()
%     A = zeros([4 4]);
%     A = add_link(A,1,3);
%     A = add_link(A,1,4);
%     A = add_link(A,2,3);
%     A = add_link(A,2,4);
%
% end


% for level=1:levels
%        for j=1:2^level
%           for i=1:nodes_in_level/2^level
%               node1 = j*nodes_in_level/2^level + i;
%               node2 = j*nodes_in_level/2^level
%           end
%        end
% end
%

function [nodes] = get_nodes_in_level(n)
    nodes = 0;
    for i=1:n
        if i*ceil(log2(i)+1) <= n
            nodes = i;
        else
            break;
        end
    end
end

function [val] = get_mod_result(start,add,n)
    val = start+add;
    if val > n
        val = mod(val,n);
    end
end
