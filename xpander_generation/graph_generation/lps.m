%% Create an expander graph denoted as LPS
% The paramaters p and q must both be prime and congurant to 1 modulo 4
function [ A ] = lps( p,q )
    if mod(p,4) ~= 1 || mod(q,4) ~= 1
        A = zeros([ 1 1 ] );
        disp('either p or q not equals 1 mod 4');
    elseif legendre_symbol(p,q) ~= 1
        A = zeros([ 1 1 ] );
        disp('the legendre symbol of p,q is not 1');
    else
        matrix_i = get_i(q);
        generators = get_generators(p,q,matrix_i);
        A = build_base_graph(generators,q);

        free_nodes = find(sum(A,2) ~= p+1);
        A = fix_graph(A,free_nodes,p+1);
    end
end

function [A] = fix_graph(A,free_nodes,d)
    modified = true;
    while modified && length(free_nodes)>1
        modified = false;
        best_gap = log(0);
        for ind_i=1:length(free_nodes)
            for ind_j=length(free_nodes)
                i = free_nodes(ind_i); j = free_nodes(ind_j);
                if i ~= j && A(i,j) ~= 1
                    B = A;
                    B(i,j) = 1; B(j,i) = 1;
                    [lambda1,lambda2] = get_spectral_gap(B,size(B,2),d);
                    if lambda1-lambda2 > best_gap
                       best_gap = lambda1-lambda2;
                       u = i;
                       v = j;
                       modified = true;
                    end
                end
            end
        end

        if modified
           A(v,u) = 1; A(u,v) = 1;
           free_nodes = find(sum(A,2) ~= d);
        end

    end
end

function [A] = build_base_graph(generators,q)
    A = zeros( [ q+1 q+1] );
    for node=0:q
        for gen=1:size(generators,2)
           neigh = get_linear_fraction(generators{gen},node,q);
           if node+1 == neigh
               continue
           end
           A(node+1, neigh) = 1;
           A(neigh,node+1) = 1;
        end
    end
end

function [neigh] = get_linear_fraction(generator,node,q)
    a = generator(1,1);
    b = generator(1,2);
    c = generator(2,1);
    d = generator(2,2);
    if node == q
        if c ~= 0
            neigh = mod( a*get_inverse(c,q),q)+1;
        else
            neigh = q+1;
        end
    elseif c~= 0 && node == -d/c
        neigh = q+1;
    else
        mone = mod(a*node + b,q);
        mechane = mod(c*node + d,q);
        if mechane == 0
            neigh = q+1;
        else
            neigh = mod(mone*get_inverse(mechane,q),q)+1;
        end
    end
end

function [inverse] = get_inverse(c,q)
    for i=0:q-1
       if mod(i*c,q) == 1
           inverse = i;
       end
    end
end

function [gen] = get_generators(p,q,i)
    gen = cell(1,1);
    cur_amnt = 1;
    for a0=1:2:p-1
        for a1=-p+1:2:p-1
           for a2=-p+1:2:p-1
                for a3=-p+1:2:p-1
                      if a0*a0 + a1*a1 + a2*a2 + a3*a3 == p
                          gen_t = zeros([2 2]);
                          gen_t(1) = mod_q_positive(a0 + (i * a1) ,q);
                          gen_t(2) = mod_q_positive(a2 + (i * a3) ,q);
                          gen_t(3) = mod_q_positive(-a2 + (i * a3) ,q);
                          gen_t(4) = mod_q_positive(a0 - (i * a1) ,q);
                          add = true;
                          for k=1:cur_amnt-1
                              if sum(sum(gen{k} == gen_t)) == 4
                                  add = false;
                              end
                          end
                          if add
                              gen{cur_amnt} = gen_t;
                              cur_amnt = cur_amnt + 1;
                          end
                      end
                end
            end
        end
    end
end

function [new_val] = mod_q_positive(val,q)
    new_val = val;
    if val < 0
        new_val = val + q;
    end
end

function [i] = get_i(q)
    for k=0:q-1
        if mod(k*k,q) == q-1
            i = k;
        end
    end
end
