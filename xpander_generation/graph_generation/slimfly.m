%% Create an expander graph denoted as LPS
% The paramaters q must be a prime power (meaning q = p^m where p is prime)
% we must have 
function [ A, d ] = slimfly( q )
    q_facts = factor(q);
    if ~all(q_facts == q_facts(1))
        disp('q must be prime power');
    end
    
    delta = mod(q,4);
    if(delta == 3)
        delta = -1;
    end
    w = (q-delta)/4;
    
    % Work in GF(p^m).
    p = q_facts(1); 
    m = length(q_facts); 
    
    chi = find_chi(p);
    
    N = 2*q^2;    
    d = (3*q-delta)/2;
    
    % Create the two sets X and XTag
    if delta == 1
        powersX = 0:2:q-3;
        powerXTag = 1:2:q-2;
        X = ones([length(powersX) 1])' .* chi .^ powersX;
        XTag = ones([length(powerXTag) 1])' .* chi .^ powerXTag;
        
    elseif delta == 0
        powersX = [0:2:2*w-2;2*w-1:2:4*w-3];
        powerXTag = [1:2:2*w-1 ;  2*w:2:4*w-2];
        X = ones(length(powersX))*chi .^ powersX ;
        XTag = ones(length(powerXTag))*chi .^ powerXTag;
        
    else
        powersX = 0:2:4*w-2;
        powerXTag = 1:2:4*w-1;
        X = ones(length(powersX))*chi .^ powersX;
        XTag = ones(length(powerXTag))*chi .^ powerXTag;
    end
    
    for i=1:length(X)
        X(i) = mod_q_positive(X(i),q);
    end
       
    for i=1:length(XTag)
        XTag(i) = mod_q_positive(XTag(i),q);
    end
    
    A = construct_slimfly(X,XTag,q);
   
    assert(sum(sum(A,2)) == N*d);
end

function nid = get_slimfly_id(a,x,y,q)
    nid = a*q^2 + q*y + x;
end

function [new_val] = mod_q_positive(val,q)
    new_val = val;
    if val < 0
        new_val = val + q;
    end
    new_val = mod(new_val,q);
end

function A = construct_slimfly(X,XTag,q)
    N = 2*q^2;    
    A = zeros([ N N ]);
    
    % map 0 -> 0
    for x = 0:q-1
        for y = 0:q-1
            for yTag = 0:q-1
                val = mod_q_positive(y-yTag,q);
                if any(X==val)
                   src = get_slimfly_id(0,x,y,q);
                   dst = get_slimfly_id(0,x,yTag,q);
                   A = add_link(A,src+1,dst+1);
                end
            end
        end
    end
    
    % map 1 -> 1
    for m = 0:q-1
        for c = 0:q-1
            for cTag = 0:q-1
                val = mod_q_positive(c-cTag,q);
                if any(XTag==val)
                   src = get_slimfly_id(1,m,c,q);
                   dst = get_slimfly_id(1,m,cTag,q);
                   A = add_link(A,src+1,dst+1);
                end
            end
        end
    end
    
    % map 0 -> 1
    for x = 0:q-1
        for m = 0:q-1
            for y = 0:q-1
                for c = 0:q-1
                    val = mod_q_positive(m*x+c,q);
                    if y==val
                       src = get_slimfly_id(0,x,y,q);
                       dst = get_slimfly_id(1,m,c,q);
                       A = add_link(A,src+1,dst+1);
                    end
                end
            end
        end
    end
    
end

function res = modulo_power(a,b,c) % Returns a^b % c
    i = 0;
    res = 1;

    while i < b
        res = mod(res*a, c);
        i = i + 1;
    end
end

function chi = find_chi(prime)
%     F=factor(prime-1); % the factors of prime-1
    for i = 2: prime-1
        chi=i;
        tag =1;
        for j= 1 :prime-1
            if (isprime(j))
                 p_i = j;
                 if(modulo_power(chi,((prime-1)/p_i),prime)== 1)
                     tag=0;
                     break 
                else
                    tag = tag +1;
                 end
            end
        end
        if (tag > 1 )
            break
        end
    end
end

