# Parameters
mean = 100 * 1000 # 100KB mean
shape = 1.05 # 1.05 shape

# Calculate scale
scale = (mean * (shape - 1)) / shape

# Pareto distribution function
pareto_cdf <- function(x) {
  1 - (scale / x)^shape
}

# Calculate data points for the plot
x = c(
  seq(1e-8, 1e-7, 1e-10), 
  seq(1e-7, 1e-6, 1e-9), 
  seq(1e-6, 1e-5, 1e-8), 
  seq(1e-5, 1e-4, 1e-7), 
  seq(1e-4, 1e-3, 1e-6), 
  seq(1e-3, 1e-2, 1e-5), 
  seq(1e-2, 1e-1, 1e-4), 
  seq(1e-1, 1e0, 1e-3), 
  seq(1e0, 1e1, 1e-2), 
  seq(1e1, 1e2, 1e-1), 
  seq(1e2, 1e3, 1e0), 
  seq(1e3, 1e4, 1e1), 
  seq(1e4, 1e5, 1e2), 
  seq(1e5, 1e6, 1e3), 
  seq(1e6, 1e7, 1e4), 
  seq(1e7, 1e8, 1e5),
  seq(1e8, 1e9, 1e6),
  seq(1e9, 1e10, 1e7),
  seq(1e10, 1e11, 1e8)
)
y = pareto_cdf(x)

# Plot without axis or functions
plot(x, y,
     log="x", 
     xlim=c(4761.9, 10e8),
     ylim=c(0, 1),
     type="l"
)

# Create data frame, filter out under zero (x > x_m)
df = data.frame(x, y)
df = df[df$y >= 0,]

# Write to result file
write.table(df, "fs_pareto_s_1.05_mu_100KB_cdf.txt", sep="\t", col.names = FALSE, row.names = FALSE)
