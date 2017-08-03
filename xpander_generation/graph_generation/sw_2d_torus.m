function adj = sw_2d_torus(n)
  N=n^2;
  adj = zeros([ N N ]);

  % create 2d torus topo
  for x=1:n
      for y=0:n-1
        adj = add_neighbors(adj,n,x,y);
      end
  end

  % now randomally select 2 more extra intermediate
  for nid=1:N
    try_cntr = 0;
    while sum(adj(nid,:)) ~= 6 && try_cntr ~= 1000
      neigh = randi([1 N]);
      
      if sum(adj(neigh,:)) < 6
        adj = add_link(adj,nid, neigh);
      end
      try_cntr = try_cntr +1;
    end
    if try_cntr == 1000
        adj = sw_2d_torus(n);
        break;
    end
  end

end


function [B] = add_neighbors(A,n, x,y)
     B = A;
     % x cord
     B = add_link(B,get_cord(x,y,n), get_cord(get_cord_pos_x(x,n),y,n));
     B = add_link(B,get_cord(x,y,n), get_cord(get_cord_neg_x(x,n),y,n));

     % y cord
     B = add_link(B,get_cord(x,y,n), get_cord(x,get_cord_pos(y,n),n));
     B = add_link(B,get_cord(x,y,n), get_cord(x,get_cord_neg(y,n),n));

end

function [cord] = get_cord(x,y,n)
    cord = x + y*n;
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
