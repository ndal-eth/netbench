function res = has_path(A,u,target,n)
    if u == target, res=1;return;end
    if nargin < 4
        n = size(A,2);
    end
    d=-1*ones(n,1);
    res = 0;
    sq=zeros(n,1); sqt=0; sqh=0; % search queue and search queue tail/head

    % start bfs at u
    sqt=sqt+1; sq(sqt)=u; 
    t=0;  
    d(u)=0; t=t+1;
    while sqt-sqh>0
        sqh=sqh+1; v=sq(sqh); % pop v off the head of the queue
        neighbors = find(A(v,:));
        for w=neighbors
            if d(w)<0
                sqt=sqt+1; sq(sqt)=w; 
                d(w)=d(v)+1; t=t+1;
                if w==target
                    res = 1;
                    return 
                end
            end
        end
    end 
end