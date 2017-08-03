%% This creates an Xpander graph using deterministic K-lifts.
% The paramaters are [switch degree, lift vector, amount of lifts]
% for example, calling lifted_xpander( 38, [2 7 3], [3 1 1] ) will create
% an Xpander graph where each switch has 38 connections to other switches and
% there are exactly: (38+1)*2*2*2*7*3=6552 switches in the graph.
% Hint: To make computations faster it is recommanded to use the higer multiplication
% first, in the above case running lifted_xpander( 38, [7 3 2], [1 1 3] ) would
% have worked much faster.

function [ A, tags ,adj_list] = lifted_xpander( d, lift_vec, lift_times )
    A = get_adjacency(d+1);
    adj_list = create_adjacency_list(A,d+1,d);
    tags = get_base_tags(d);

    assert(length(lift_vec)==length(lift_times));

    for ind=1:length(lift_vec)
        disp(['now doing lift with k=', int2str(lift_vec(ind)), ...
                        ' and times=',int2str(lift_times(ind))]);
       [A,tags, adj_list] = k_extension( A,d,lift_vec(ind),lift_times(ind), tags, adj_list);
    end
end

function [tags] = get_base_tags(d)
    tags = cell(d+1,1);
    for i=1:d+1
        tags{i} = int2str(i);
    end
end
