function list = create_adjacency_list(A,n,d)
    list = zeros([n*d/2 2] ) ;
    i = 1;
    for row = 1:n
      for col = row+1:n
          if A(row,col) == 1
              list(i, 1:2) = [row,col];
              i = i+1;
          end
      end
    end
    list( ~any(list,2), : ) = [];
end