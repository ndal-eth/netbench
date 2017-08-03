function [ res] = compare_expansion( mat1, mat2, d )
    gap = d - 2*sqrt(d-1);
    lambda_mat1 = sort(abs(eig(mat1)));
    lambda_mat2 = sort(abs(eig(mat2)));
%     disp([lambda_mat1(length(lambda_mat1)) lambda_mat2(length(lambda_mat2))]);
%     disp( lambda_mat1(length(lambda_mat1))  lambda_mat2(length(lambda_mat2)))
    gap1 = lambda_mat1(length(lambda_mat1))-lambda_mat1(length(lambda_mat1)-1);
    gap2 = lambda_mat2(length(lambda_mat2))-lambda_mat2(length(lambda_mat2)-1);
    
    res= gap1/gap2;

end

