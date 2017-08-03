function [adj,lambda1,lambda2, list] = xpander_heuristic(adj,n, d, list, gap_func)
   resultsTemp = zeros([n*d/2 1]);

    parfor i = 1:n*d/2
        row = list(i,1);
        col = list(i,2);
        gap = getGapLoop(adj,row,col, gap_func);
        %disp([row col gap]);
       resultsTemp(i) = gap;
    end

    results = zeros([n*d/2 2]);
    for i = 1:n*d/2
       results(i, 1:2) = [i, resultsTemp(i)];
    end

    sortedResults = sortrows(results,2);
    %disp(sortedResults);
   neighs = zeros( [1 n] );
   cntr = 0;
   size = n*d/2+1;
   for ind = n*d/2:-1:1
       % break if we got our neighbors
       if cntr == d
           break;
       else
          listInd =sortedResults(ind,1);
          neigh1 =  list(listInd,1);
          neigh2 =  list(listInd,2);
          if neighs(neigh1) == 0 && neighs(neigh2) == 0
              neighs(neigh1) = 1;
              neighs(neigh2) = 1;
                
              disp([neigh1 neigh2]);
              % make room
              adj(neigh1,neigh2) = 0;
              adj(neigh2,neigh1) = 0;

              % reconnect
              adj(n+1,neigh1) = 1;
              adj(n+1,neigh2) = 1;
              adj(neigh1,n+1) = 1;
              adj(neigh2,n+1) = 1;

              list(listInd, 1:2) = [n+1,neigh1];
              list(size+cntr/2, 1:2) = [neigh2, n+1];
              cntr = cntr+2;
          end
       end
   end
   %B = adj;
   [lambda1,lambda2] = get_spectral_gap(adj,n+1,d);
end

function gap = getGapLoop(adj,row,col,gap_func)
        adj(row,col) = 0 ; adj(col,row) = 0;
        gap = gap_func(adj);
end
