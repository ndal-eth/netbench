function factors = get_2_factors(A,n,d)
    if nargin<2
       n = size(A,2);
       d = sum(sum(A,2))/n;
    end
    if mod(d,2) ~= 0
        factors = 0;
        disp('Graph degree should be 2*k for some value of k');
        return;
    end
    path = transpose(euler_path(A,n,d));
    %factors = zeros(d/2, n);
    factors = containers.Map('KeyType','int32','ValueType', 'any');
    % new matrix to hold the bipartite graphs, each v \in A is now v_1, v_2
    A2 = zeros(2*n, 2*n);
    counter = 0;
    for i=1:length(path)-1
        v = path(i);
        u = path(i+1);
        A2(v, n+u) = 1;
        A2(n+u, v) = 1;
    end
    while counter ~= d/2
        counter = counter +1;
        % Dulmage-Mendelsohn decomposition - find a perfect matching
        p = dmperm(A2);
        A3 = zeros(n, n);
        for i=1:n
            if p(i)==0, continue; end
            v = int32(i);
            u = int32(p(i)-n);
            A3(v,u)=1;A3(u,v)=1;
            A2(p(i),i)=0;A2(i,p(i))=0;
        end
        % find strongly connected componnents
        [S,C] = graphconncomp(sparse(A3),'Directed',false);
        factor = containers.Map('KeyType','int32','ValueType','any');
        if S == 1
            % We have only one path (hemiltonian one)
            factor_vec = transpose(euler_path(A3,n,2));
            factor(1) = factor_vec(1:n);
        else
            % create a circle of each connected component
            for i=1:S
                nodes_in_comp = find(C==i);
                circle = zeros(1,length(nodes_in_comp));

                % get euler path for reduced matrix
                circle_meta = euler_path(A3(nodes_in_comp,nodes_in_comp),length(nodes_in_comp),2);

                % convert to actual path
                for j=1:length(nodes_in_comp)
                    circle(j)=nodes_in_comp(circle_meta(j));
                end

                factor(i) = circle;
            end
        end
        factors(counter) = factor;

    end
    %disp(factors);
    if ~validate_factors(A,n,d,factors)
        disp('something bad happend!');
    else
        disp('Factors are alright!!')
    end
end
function res = validate_factors(A,n,d,factors)
    factored_A = zeros([n n]);
    for factor_ind=1:factors.Count
        factor = factors(factor_ind);
       for circle_ind=1:factor.Count
           circle = factor(circle_ind);
           for i=1:length(circle)-1
               factored_A(circle(i),circle(i+1)) = 1;
               factored_A(circle(i+1),circle(i)) = 1;
           end
           factored_A(circle(length(circle)),circle(1)) = 1;
           factored_A(circle(1),circle(length(circle))) = 1;
       end
    end
    
    res = isequal(A,factored_A);
end

