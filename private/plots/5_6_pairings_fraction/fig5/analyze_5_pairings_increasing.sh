#!/usr/bin/env bash

# INCREASING ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=5_pairings_increasing
LABEL=5_pairings_increasing

# Combine results
python ../../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_mean_fct_ms.txt
python ../../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt
python ../../../../analysis/multi_combine.py all_flows_completed_fraction flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_flow_compl.txt

# Plot
gnuplot plot_5a_pairings_increasing_avg_fct.plt
gnuplot plot_5b_pairings_increasing_99th_fct_short.plt
gnuplot plot_5c_pairings_increasing_throughput_large.plt
gnuplot plot_5d_pairings_increasing_flow_compl.plt
