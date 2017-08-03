function [A] = hypercube(nodes)
  if ~is_power_two(nodes):
    A = [];
    return;
  end
  n = log2(nodes);

end


function res = is_power_two(n)
  res = log2(n) == floor(log2(n));
end
