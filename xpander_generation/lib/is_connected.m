function connected = is_connected( adj , n)
    % setting n as optional param (for legacy code)
    if nargin < 2
        n = size(adj,2);
    end
    total = n*(n-1)/2;
    res = zeros([total 1]);
    count = 1;
    for i=1:n
        for j=i+1:n
            res(count) = has_path(adj,i,j,n);
            count = count + 1;
        end
    end
    count = count -1;
    connected = count == sum(res);
end

