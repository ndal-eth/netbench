function [A] = add_link(A,s,t)
%     disp([s t]);
    A(s,t) = 1;
    A(t,s) = 1;
end

