#!/usr/bin/env bash

# PARETO INCREASING FRACTION

FOLDER_IN_RESULTS=8_flowsize_pareto/fixed_R_increasing_pairings_fraction
LABEL=8_flowsize_pareto_pairings

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > 8a/data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > 8a/data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > 8a/data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt

# Plot
cd 8a
gnuplot plot_8a1_flowsize_pareto_pairings_avg_fct.plt
gnuplot plot_8a2_flowsize_pareto_pairings_99th_fct_short.plt
gnuplot plot_8a3_flowsize_pareto_pairings_throughput_large.plt
cd ..

# A2A INCREASING FRACTION

FOLDER_IN_RESULTS=8_flowsize_pareto/fixed_R_increasing_a2a_fraction
LABEL=8_flowsize_pareto_a2a

# Combine results
python ../../../analysis/multi_combine.py all_mean_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > 8b/data_${LABEL}_mean_fct_ms.txt
python ../../../analysis/multi_combine.py less_100KB_99th_fct_ms flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > 8b/data_${LABEL}_less_100KB_99th_fct_ms.txt
python ../../../analysis/multi_combine.py geq_100KB_throughput_mean_Gbps flow_completion.statistics ../../../temp/results/${FOLDER_IN_RESULTS} actservers actservers > 8b/data_${LABEL}_geq_100KB_throughput_mean_Gbps.txt

# Plot
cd 8b
gnuplot plot_8b1_flowsize_pareto_pairings_avg_fct.plt
gnuplot plot_8b2_flowsize_pareto_pairings_99th_fct_short.plt
gnuplot plot_8b3_flowsize_pareto_pairings_throughput_large.plt
cd ..

