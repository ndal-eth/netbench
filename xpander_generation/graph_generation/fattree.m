%% This creates a FatTree graph with uniform degree of d
function [A] = fattree( d )
    tors = d^2/2;
    aggs = d^2/2;
    cores = d^2/4;
    nodes = tors+aggs+cores;
    A = zeros( [ nodes nodes] );


    % connect tors to aggregators (pod by pod)
    for pod=1:d
        for agg=1:d/2
            for tor=1:d/2
                A = add_link(A,(pod-1)*d/2 + tor, tors + (pod-1)*d/2+agg);
            end
        end
    end

    % connect cores to aggregators (pod by pod)
    for core=1:cores
        for pod=1:d
            agg=mod(core,d/2);
            if agg==0
                agg = d/2;
            end
            A = add_link(A,tors+aggs+core,tors+(pod-1)*d/2+agg);
        end
    end

end
