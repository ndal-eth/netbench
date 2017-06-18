#!/usr/bin/env bash

#!/usr/bin/env bash

# DUAL

FOLDER_IN_RESULTS=17_duallarge
LABEL=17_duallarge

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt
python ../../../analysis/multi_combine.py all_flows_completed_fraction flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_flow_compl.txt

# Plot
gnuplot plot_17a_duallarge_avg_fct.plt
gnuplot plot_17b_duallarge_99th_fct_short.plt
gnuplot plot_17c_duallarge_throughput_large.plt
gnuplot plot_17d_duallarge_flow_compl.plt