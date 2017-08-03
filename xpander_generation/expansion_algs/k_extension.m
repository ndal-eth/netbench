function [ tmp_lift, tmp_tags, list] = k_extension( A,d,k,times, tags, list)    
    tmp_lift = A;
    tmp_tags = tags;
    for i=1:times
        disp(['doing lift number: ', num2str(i), ' out of: ',num2str(times)]);
        [tmp_lift, tmp_tags, list] = do_lifting(tmp_lift,d,k, tmp_tags, list);
    end
end

function [ tmp_lift,tmp_tags, res_list ] = do_lifting( A ,d, k,tags, list)
    [tmp_lift, tmp_tags] = get_tmp_lift(A,k,tags, list);
    disp(['got tmp lift, now improving, tmp is connected? never mind!']);%, ...
%                 num2str(is_connected(tmp_lift))]);
 
    improve = true;
    
    while improve
        improve = false;
        for i=1:length(list)
            org_src = list(i,1);
            org_dst = list(i,2);
            cpy_list = tmp_lift;
            % try every possible change
            for lift_ind=1:k
%                 cntr = cntr +1;
                cur_node = (org_src-1)*k+lift_ind; 
                neighbor = find(cpy_list(cur_node, (org_dst-1)*k+1:org_dst*k));
                neighbor = (org_dst-1)*k + neighbor;
                % verify that they are actually neighbors
                assert(cpy_list(cur_node,neighbor)==1);
                
                for lift=1:k
                    if lift == lift_ind
                        continue;
                    end
                    
                    other_node = (org_src-1)*k+lift; 
                    other_neighbor = find(cpy_list(other_node, (org_dst-1)*k+1:org_dst*k));
                    other_neighbor = (org_dst-1)*k + other_neighbor;
                    % verify that they are actually neighbors
                    assert(cpy_list(other_node,other_neighbor)==1);
                    if is_neighbor(cpy_list,other_node,neighbor) || ...
                        is_neighbor(cpy_list,cur_node,other_neighbor)
                        continue;
                    end
                    % now do the switch
                    % first remove
                    cpy_list = remove_link(cpy_list,other_node,other_neighbor);
                    cpy_list = remove_link(cpy_list,cur_node,neighbor);
                    
                    % now add
                    cpy_list = add_link(cpy_list,other_node,neighbor);
                    cpy_list = add_link(cpy_list,cur_node,other_neighbor);
                    
                    old_gap = get_gap(tmp_lift);
                    new_gap = get_gap(cpy_list);
%                         disp([old_gap new_gap]);
                    if new_gap>old_gap
                       tmp_lift = cpy_list;
                       improve = true;
%                        disp('improved');
                       break;
                    else
                        cpy_list = tmp_lift;
                    end
                end              
            end
        end
%         disp('done one iteration');
    end
    res_list = create_adjacency_list(tmp_lift,size(tmp_lift,2),d);
end

function [gap] = get_gap(A)
    [lambda1, lambda2] = get_spectral_gap(A,size(A,2),0);
    gap  = lambda1 - lambda2;
end

function [tmp_lift] = get_tmp_lift_1(A, k, start_val)
    % using random permutations for each edge
    n = size(A,2);
    d = sum(sum(A,2))/n;
    list = create_adjacency_list(A,size(A,2),d);
%     for perm_ind=1:size(all_perms)
    tmp_lift = zeros([ k*n k*n]);

    for i=1:length(list)
        perm = randperm(k);
        row = list(i,1);
        col = list(i,2);
        for lift=1:k
              tmp_lift = add_link(tmp_lift, (row-1)*k+lift,(col-1)*k+perm(lift));
        end
    end

    if ~is_connected(tmp_lift)
        disp('failed to stay connected with permutations');
        tmp_lift = get_tmp_lift(A, k, start_val);
    end
    
end

% this is deterministic
function [tmp_lift] = get_tmp_lift_2(A, k, start_val)
    n = size(A,2);
    d = sum(sum(A,2))/n;
    all_perms = perms(1:k);
    list = create_adjacency_list(A,size(A,2),d);
    for perm_ind=1:size(all_perms)
        tmp_lift = zeros([ k*n k*n]);
        perm = all_perms(perm_ind,:);
        for i=1:length(list)
            row = list(i,1);
            col = list(i,2);
            for lift=1:k
                  tmp_lift = add_link(tmp_lift, (row-1)*k+lift,(col-1)*k+perm(lift));
            end
        end

        if is_connected(tmp_lift)
            disp('found the good permutation: ');
            disp(perm);
            break;
        else
            disp('failed to stay connected with permutations: ');
            disp(perm);
        end
    end
end

function [tmp_lift, tmp_tags] = get_tmp_lift(A, k, tags, list)
    if mod(k,2)==0
        func = @get_tmp_lift_even;
    else
        func = @get_tmp_lift_odd;
    end
    [tmp_lift,tmp_tags] = func(A,k,tags,list);
end

function [tmp_lift, tmp_tags] = get_tmp_lift_odd(A, k, tags, list)
    n = size(A,2);
    tmp_lift = zeros([ k*n k*n]);
    tmp_tags = cell(size(tmp_lift,2),1);
    
    for node=1:n
       for lift=1:k
          tmp_tags{(node-1)*k+lift} = tags{node}; 
       end
    end
    
    for i=1:length(list)
        row = list(i,1);
        col = list(i,2);
        perm = randperm(k);
%         if mod(k,2)==0
%             perm = perm1;
%         else
%             if mod(i,3) == 0
%                 perm = perm1;
%             else
%                 perm = perm2;
%             end
%         end
        
        for lift=1:k
              tmp_lift = add_link(tmp_lift, (row-1)*k+lift,(col-1)*k+perm(lift));
        end
    end
end

function [tmp_lift, tmp_tags] = get_tmp_lift_even(A, k, tags, list)
    n = size(A,2);
    tmp_lift = zeros([ k*n k*n]);
    tmp_tags = cell(size(tmp_lift,2),1);
    
    perm1 = zeros([1 k]);
    perm2 = zeros([1 k]);
%     if mod(k,2)==0
        perm1(1:k-1)=2:k;
        perm1(k) = 1;
%     else
        perm2(1:k-2)=3:k;
        perm2(k-1) = 1;
        perm2(k) = 2;
%     end
    for node=1:n
       for lift=1:k
          tmp_tags{(node-1)*k+lift} = tags{node}; 
       end
    end
    
    for i=1:length(list)
        row = list(i,1);
        col = list(i,2);
        perm = randperm(k);
        if mod(k,2)==0
            perm = perm1;
        else
            if mod(i,3) == 0
                perm = perm1;
            else
                perm = perm2;
            end
        end
        
        for lift=1:k
              tmp_lift = add_link(tmp_lift, (row-1)*k+lift,(col-1)*k+perm(lift));
        end
    end
end
