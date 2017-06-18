#!/usr/bin/env bash

# INCREASING ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=3_a2a_increasing
LABEL=3_a2a_increasing

# Combine results
python ../../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_mean_fct_ms.txt
python ../../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt
python ../../../../analysis/multi_combine.py all_flows_completed_fraction flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_flow_compl.txt

# Plot
gnuplot plot_3a_a2a_increasing_avg_fct.plt
gnuplot plot_3b_a2a_increasing_99th_fct_short.plt
gnuplot plot_3c_a2a_increasing_throughput_large.plt
