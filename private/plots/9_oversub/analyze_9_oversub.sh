#!/usr/bin/env bash

# INCREASING ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=9_oversub
LABEL=9_oversub

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt
python ../../../analysis/multi_combine.py all_flows_completed_fraction flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_flow_compl.txt

# Plot
gnuplot plot_9a_oversub_avg_fct.plt
gnuplot plot_9b_oversub_99th_fct_short.plt
gnuplot plot_9c_oversub_throughput_large.plt
gnuplot plot_9d_oversub_flow_compl.plt
