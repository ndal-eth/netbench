function [A] = load_sparse_mat(file_path)
    mat = load(file_path);
    A = spconvert(mat);
    %A = full(A_sp);
end

