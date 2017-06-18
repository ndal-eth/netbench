#!/usr/bin/env bash

# PARETO SKEW

FOLDER_IN_RESULTS=7_tm_pareto_skew
LABEL=7_tm_pareto_skew

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} skew skew > data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} skew skew > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} skew skew > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt

# Plot
gnuplot plot_7a_tm_pareto_skew_avg_fct.plt
gnuplot plot_7b_tm_pareto_skew_99th_fct_short.plt
gnuplot plot_7c_tm_pareto_skew_throughput_large.plt

