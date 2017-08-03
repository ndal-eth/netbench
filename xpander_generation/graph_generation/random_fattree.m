function [ A ] = random_fattree( d )
    
    tors = d;
    aggs = d;
    cores = d/2;
    nodes = tors+aggs+cores;
    A = zeros( [ nodes nodes] );
    
%     tors_vec = repmat(1 : d^2/2, 1 , d/2);
%     perm_tors = randperm(length(tors_vec));
%     free_aggs = [1:aggs]+tors;
%     free_cores = [1:cores]+aggs+tors;
%     add_multiple_links
    
    no_good=1;
    while no_good
        no_good=0;
        temp_A = A;
        for tor=1:tors
           now = sum(temp_A,2);
           free_aggs = find(now(tors+1:tors+aggs)<d/2);
           if length(free_aggs) < d/2
               no_good=1;
               break;
           end
           neighs = datasample(free_aggs,d/2,'Replace',false);
    %        disp(neighs);
           for n=neighs
               temp_A = add_link(temp_A,tor,aggs+n);
           end
        end
        if ~no_good
            disp('done tors');
        end
    end
    A = temp_A;
    
%     for core=1:cores
%         for pod=1:d
%             agg=mod(core,d/2);
%             if agg==0
%                 agg = d/2;
%             end
%             A = add_link(A,tors+aggs+core,tors+(pod-1)*d/2+agg);
%         end
%     end
    
    no_good=1;
    while no_good
        no_good=0;
        temp_A = A;
        for agg=1:aggs
           now = sum(temp_A,2);
           free_cores = find(now(tors+aggs+1:length(now))<d);
           if length(free_cores) < d/2
               no_good=1;
               break;
           end
           neighs = datasample(free_cores,d/2,'Replace',false);
           for n=neighs
               temp_A = add_link(temp_A,tors+agg,tors+aggs+n);
           end
        end
        if ~no_good
            disp('done cores');
        end
    end
    A=temp_A;
end
