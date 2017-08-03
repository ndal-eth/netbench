function [ A ] = add_multiple_links( A,s,t )
    A(s,t) = A(s,t) + 1;
    A(t,s) = A(t,s) + 1;
end


