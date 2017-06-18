#!/usr/bin/env bash

# INCREASING ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=6_pairings_30perc
LABEL=6_pairings_30perc

# Combine results
python ../../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_mean_fct_ms.txt
python ../../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../../temp/results/${FOLDER_IN_RESULTS} flows flows > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt

# Plot
gnuplot plot_6a_pairings_30perc_avg_fct.plt
gnuplot plot_6b_pairings_30perc_99th_fct_short.plt
gnuplot plot_6c_pairings_30perc_throughput_large.plt
