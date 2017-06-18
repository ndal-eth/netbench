#!/usr/bin/env bash

# INCREASING ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=4_a2a_30perc
LABEL=4_a2a_30perc

# Combine results
python ../../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_mean_fct_ms.txt
python ../../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt

# Plot
gnuplot plot_4a_a2a_30perc_avg_fct.plt
gnuplot plot_4b_a2a_30perc_99th_fct_short.plt
gnuplot plot_4c_a2a_30perc_throughput_large.plt
