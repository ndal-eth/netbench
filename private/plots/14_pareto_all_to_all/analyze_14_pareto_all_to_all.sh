#!/usr/bin/env bash

# INCREASING ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=14_pareto_all_to_all/30perc
LABEL=14_pareto_all_to_all

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows actservers > data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows actservers > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows actservers > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt
python ../../../analysis/multi_combine.py all_flows_completed_fraction flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows actservers > data_${LABEL}_flow_compl.txt

# Plot
gnuplot plot_14a_pareto_all_to_all_avg_fct.plt
gnuplot plot_14b_pareto_all_to_all_99th_fct_short.plt
gnuplot plot_14c_pareto_all_to_all_throughput_large.plt
gnuplot plot_14d_pareto_all_to_all_flow_compl.plt
