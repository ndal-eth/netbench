function adj = sw_ring(n)
  adj = zeros([ n n ]);

  % add ring
  for i=1:n-1
    adj = add_link(adj,i,i+1);
  end
  adj = add_link(adj,n,1);

  % now randomally select 4 extra intermediate
  for nid=1:n
    try_cntr = 0;
    while sum(adj(nid,:)) ~= 6 && try_cntr ~= 1000
      neigh = randi([1 n]);
      
      if sum(adj(neigh,:)) < 6
        adj = add_link(adj,nid, neigh);
      end
      try_cntr = try_cntr +1;
    end
    if try_cntr == 1000
        adj = sw_ring(n);
        break;
    end
  end
end
