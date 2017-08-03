function [ dist, distro ] = get_avg_hosts_length( A, tors, hosts_per_tor, max_length )
    
%     dist = 0;
    shortest = graphallshortestpaths(sparse(A),'directed',false);
    total_hosts = hosts_per_tor*length(tors);
    distro = zeros([ max_length+1 1]);
    for t1=tors
        for t2=tors
            if t1==t2
                distro(max_length+1) = distro(max_length+1) + hosts_per_tor^2;
                continue
            end
            distro(shortest(t1,t2)) = distro(shortest(t1,t2)) + hosts_per_tor^2;
%             dist = shortest(t1,t2)*(hosts_per_tor^2-hosts_per_tor);
        end
    end
    
%     dist = dist;%/(total_hosts^2-total_hosts);
%     dist/
    distro = distro/total_hosts^2;
    dist = 0;
    for i = 1:max_length
        dist = dist + distro(i)*(i+1);
    end
    dist = dist + distro(max_length+1);

   
end

