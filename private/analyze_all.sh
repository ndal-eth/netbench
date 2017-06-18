#!/usr/bin/env python

cd ..
cd analysis
screen -d -m python multi_analyze_1s.py ../temp/results/13_pareto_pairings/30perc
screen -d -m python multi_analyze_1s.py ../temp/results/13_pareto_pairings/full
screen -d -m python multi_analyze_1s.py ../temp/results/14_pareto_all_to_all/30perc
screen -d -m python multi_analyze_1s.py ../temp/results/15_pareto_a2a_fraction
screen -d -m python multi_analyze_1s.py ../temp/results/8_flowsize_pareto/fixed_R_increasing_a2a_fraction
screen -d -m python multi_analyze_1s.py ../temp/results/8_flowsize_pareto/fixed_R_increasing_pairings_fraction
echo "DEPLOYED SCREENS"
screen -ls
echo "DOING REST SEQUENTIAL"
python multi_analyze_1s.py ../temp/results/1_failcases/tiny_frac
python multi_analyze_1s.py ../temp/results/1_failcases/large_frac
echo "FINISHED 1"
python multi_analyze_1s.py ../temp/results/3_a2a_increasing
echo "FINISHED 2"
python multi_analyze_1s.py ../temp/results/4_a2a_30perc
echo "FINISHED 3"
python multi_analyze_1s.py ../temp/results/5_pairings_increasing
echo "FINISHED 4"
python multi_analyze_1s.py ../temp/results/6_pairings_30perc
echo "FINISHED 5"
python multi_analyze_1s.py ../temp/results/7_tm_pareto_skew
echo "FINISHED 6"
python multi_analyze_1s.py ../temp/results/9_oversub
echo "FINISHED 7"
python multi_analyze_1s.py ../temp/results/10_projector
echo "FINISHED 8"
python multi_analyze_1s.py ../temp/results/11_fixed_R_a2a_fraction
echo "FINISHED 9"
python multi_analyze_1s.py ../temp/results/12_fixed_R_pairings_fraction
echo "FINISHED 10"
python multi_analyze_1s.py ../temp/results/16_dual
echo "FINISHED 11"
python multi_analyze_1s.py ../temp/results/17_duallarge
echo "FINISHED 12"
