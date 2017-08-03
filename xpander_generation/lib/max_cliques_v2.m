function [MC] = max_cliques_v2(A)

n = size(A,2); % number of vertices
MC = cell(n,1); % storage for maximal cliques
R = false(n,1); % currently growing clique
P = true(n,1); % prospective nodes connected to all nodes in R
X = false(n,1); % nodes already processed
iclique=0;
A=A.'; %this speeds up some of the calculations below because we do not have to transpose A for each recursion

%% run
bron_kerbosch(R,P,X);

%% trim output
MC((iclique+1):end)=[];

    function [] = bron_kerbosch( R, P, X )
        
        if ~any(P | X)
            % report R as a maximal clique
            iclique=iclique+1;
            MC{iclique}=find(R);
        else
            % choose pivot
            ppivots = P | X; % potential pivots
            binP = zeros(1,n);
            binP(P) = 1; % binP contains ones at indices equal to the values in P
            pcounts = binP*double(A(:,ppivots)); % cardinalities of the sets of neighbors of each ppivots intersected with P
            % select one of the ppivots with the largest count
            [~,ind] = max(pcounts);
            temp_u=find(ppivots,ind,'first');
            u_p=temp_u(ind);
            
            for u = find(~A(:,u_p) & P).' % all prospective nodes who are not neighbors of the pivot
                P(u)=false;
                Rnew = R;
                Rnew(u)=true;
                Nu = A(:,u);
                Pnew = P & Nu;
                Xnew = X & Nu;
                bron_kerbosch(Rnew, Pnew, Xnew);
                X(u)=true;
            end
        end
        
    end % BKv2
end % maximalCliques