function [ res ] = compare_lambda2( mat1,mat2 )
    lambda_mat1 = sort(abs(eig(mat1)));
    lambda_mat2 = sort(abs(eig(mat2)));
    res = lambda_mat1(length(lambda_mat1)-1)/lambda_mat2(length(lambda_mat2)-1);
end

