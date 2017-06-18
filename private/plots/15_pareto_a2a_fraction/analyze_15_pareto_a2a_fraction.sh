#!/usr/bin/env bash

# A2A INCREASING FRACTION

FOLDER_IN_RESULTS=15_pareto_a2a_fraction
LABEL=15_pareto_a2a_fraction

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt

# Plot
gnuplot plot_15a_pareto_a2a_fraction_avg_fct.plt
gnuplot plot_15b_pareto_a2a_fraction_99th_fct_short.plt
gnuplot plot_15c_pareto_a2a_fraction_throughput_large.plt
