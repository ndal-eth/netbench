#!/usr/bin/env bash

# INCREASING ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=13_pareto_pairings/30perc
LABEL=13_pareto_pairings

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows actservers > data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows actservers > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows actservers > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt

# Plot
gnuplot plot_13a_pareto_pairings_avg_fct.plt
gnuplot plot_13b_pareto_pairings_99th_fct_short.plt
gnuplot plot_13c_pareto_pairings_throughput_large.plt
