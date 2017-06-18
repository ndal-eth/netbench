#!/usr/bin/env bash

#############################
# TINY ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=1_failcases/tiny_frac
LABEL=1_failcase_ecmp

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows actservers > data_${LABEL}_mean_fct_ms.txt

# Plot
gnuplot plot_1a_failcase_ecmp_avg_fct.plt


#############################
# LARGE ALL-TO-ALL FRACTION

FOLDER_IN_RESULTS=1_failcases/large_frac
LABEL=1_failcase_vlb

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} flows actservers > data_${LABEL}_mean_fct_ms.txt

# Plot
gnuplot plot_1b_failcase_vlb_avg_fct.plt
