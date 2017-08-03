function [A] = remove_link(A,s,t)
    A(s,t) = 0;
    A(t,s) = 0;
end