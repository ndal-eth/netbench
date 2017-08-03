function [ symbol ] = legendre_symbol( a, p )
    if mod(a,p) == 0
        symbol = 0;
    else
        symbol = -1;
        for i=0:p-1
            if mod(i*i,p) == mod(a,p)
                symbol = 1;
            end
        end
    end

end

