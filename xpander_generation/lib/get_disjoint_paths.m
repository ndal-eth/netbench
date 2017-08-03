function [ paths ] = get_disjoint_paths( A, tors )
    paths = length(tors);
    A = sparse(A);
    for src=1:length(tors)
        for dst=src+1:length(tors)
            paths_t = get_num_paths(A,tors(src),tors(dst));
            disp([ tors(src) tors(dst) paths_t]);
            if paths_t < paths
                paths = paths_t;
            end
        end
    end
end

function [res] = get_num_paths(A,s,t)
   res = 0;
   while has_path(A,s,t)
      [dist, path, pred] = graphshortestpath(A, s, t, 'Directed', false); 
      for i=1:length(path)-1
          p1 = path(i);
          p2 = path(i+1);
          A = remove_link(A,p1,p2);
      end
      res = res+1;
   end

end