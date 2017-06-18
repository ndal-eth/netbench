#!/usr/bin/env bash

#!/usr/bin/env bash

# INCREASING ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=10_projector
LABEL=10_projector

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt
python ../../../analysis/multi_combine.py all_flows_completed_fraction flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_flow_compl.txt

# Plot
gnuplot plot_10a_projector_no_bottleneck_avg_fct.plt
gnuplot plot_10b_projector_no_bottleneck_99th_fct_short.plt
gnuplot plot_10c_projector_no_bottleneck_throughput_large.plt
gnuplot plot_10d_projector_with_bottleneck_avg_fct.plt
gnuplot plot_10e_projector_with_bottleneck_99th_fct_short.plt
gnuplot plot_10f_projector_with_bottleneck_throughput_large.plt
gnuplot plot_10g_projector_with_bottleneck_flow_compl.plt
gnuplot plot_10h_projector_no_bottleneck_flow_compl.plt
