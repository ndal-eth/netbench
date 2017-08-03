%% This creates a 3D-Torus with n^3 switches each of degree 6
function [ A ] = three_d_torus( n )
    A = zeros([ n^3 n^3]);
    for x=1:n
        for y=0:n-1
            for z=0:n-1
                A = add_neighbors(A,n,x,y,z);
            end
        end
    end
end

function [B] = add_neighbors(A,n, x,y,z)
     B = A;
     % x cord
     B = add_link(B,get_cord(x,y,z,n), get_cord(get_cord_pos_x(x,n),y,z,n));
     B = add_link(B,get_cord(x,y,z,n), get_cord(get_cord_neg_x(x,n),y,z,n));

     % y cord
     B = add_link(B,get_cord(x,y,z,n), get_cord(x,get_cord_pos(y,n),z,n));
     B = add_link(B,get_cord(x,y,z,n), get_cord(x,get_cord_neg(y,n),z,n));

     % z cord
     B = add_link(B,get_cord(x,y,z,n), get_cord(x,y,get_cord_pos(z,n),n));
     B = add_link(B,get_cord(x,y,z,n), get_cord(x,y,get_cord_neg(z,n),n));

end

function [cord] = get_cord(x,y,z,n)
    cord = x + y*n + z*n^2;
end

function [cord] = get_cord_pos(org,n)
    if org == n-1
        cord = 0;
    else
        cord = org+1;
    end
end

function [cord] = get_cord_neg(org,n)
    if org == 0
        cord = n-1;
    else
        cord = org-1;
    end
end

function [cord] = get_cord_pos_x(org,n)
    if org == n
        cord = 1;
    else
        cord = org+1;
    end
end

function [cord] = get_cord_neg_x(org,n)
    if org == 1
        cord = n;
    else
        cord = org-1;
    end
end
