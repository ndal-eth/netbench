function [ tmp_lift ] = two_lifting( A ,d,times)    
    tmp_lift = A;
    for i=1:times
        disp(['doing lift number: ', num2str(i), ' out of: ',num2str(times)]);
        tmp_lift = do_lifting(tmp_lift,d);
    end
end

function [ tmp_lift ] = do_lifting( A ,d)
    tmp_lift = get_tmp_lift(A,2);
    improve = true;
    list = create_adjacency_list(A,size(A,2),d);
    cur_size = size(A,2);
    
    while improve
        improve = false;
        for i=1:length(list)
            from = list(i,1);
            to = list(i,2);
            B = tmp_lift;
            % this means a form of || we want X
            if B(from,to) == 1
                % remove old link
                B = remove_link(B,from,to);
                B = remove_link(B,to+cur_size,from+cur_size);
                
                % attach the new link
                B = add_link(B,from,to+cur_size);
                B = add_link(B,to,from+cur_size);
                
            
            else
                % this means a form of X we want ||
                % remove old link
                B = add_link(B,from,to);
                B = add_link(B,to+cur_size,from+cur_size);
                
                % attach the new link
                B = remove_link(B,from,to+cur_size);
                B = remove_link(B,to,from+cur_size);
            end
            old_gap = get_gap(tmp_lift);
            new_gap = get_gap(B);
            if new_gap>old_gap
               %disp(new_gap);
               tmp_lift = B;
               improve = true;
            end
        end
    end
end

function [gap] = get_gap(A)
    [lambda1, lambda2] = get_spectral_gap(A,size(A,2),0);
    gap  =lambda1- lambda2;
end

function [tmp_lift] = get_tmp_lift(A, mod_val)
    n = size(A,2);
    tmp_lift = zeros([ 2*n 2*n]);
    tmp_lift(1:n,1:n) = A;
    link = 0;
    for row=1:n
        for col=row+1:n
            if A(row,col) == 1
                link = mod(link+1,mod_val);
                if ~link
                    % do an X connection
                    tmp_lift = remove_link(tmp_lift,row,col);
                    tmp_lift = add_link(tmp_lift,row,col+n);
                    tmp_lift = add_link(tmp_lift,col,row+n);
                else
                    % do an || connection
                    % just need to add the other side
                    tmp_lift = add_link(tmp_lift,col+n,row+n);
                end
            end
        end
    end
    
    if ~is_connected(tmp_lift,2*n)
        disp(['not connected, trying mod val of: ', num2str(mod_val)]);
        tmp_lift = get_tmp_lift(A,mod_val+1);
    end
end
