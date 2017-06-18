#!/usr/bin/env bash

#!/usr/bin/env bash

# DUAL

FOLDER_IN_RESULTS=16_dual
LABEL=16_dual

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt
python ../../../analysis/multi_combine.py all_flows_completed_fraction flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_flow_compl.txt

# Plot
gnuplot plot_16a_dual_avg_fct.plt
gnuplot plot_16b_dual_99th_fct_short.plt
gnuplot plot_16c_dual_throughput_large.plt
gnuplot plot_16d_dual_no_bottleneck_avg_fct.plt
gnuplot plot_16e_dual_no_bottleneck_99th_fct_short.plt
gnuplot plot_16f_dual_no_bottleneck_throughput_large.plt
gnuplot plot_16g_dual_no_bottleneck_flow_compl.plt
gnuplot plot_16h_dual_flow_compl.plt