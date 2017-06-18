#!/usr/bin/env bash

LBL=1_failcases
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=3_4_all_to_all_fraction
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=5_6_pairings_fraction
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=7_tm_pareto_skew
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=8_flowsize_pareto
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=9_oversub
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=10_projector
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=11_fixed_R_a2a_fraction
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=12_fixed_R_pairings_fraction
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=13_pareto_pairings
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=14_pareto_all_to_all
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=15_pareto_a2a_fraction
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=16_dual
cd ${LBL}
bash analyze_${LBL}.sh
cd ..

LBL=17_duallarge
cd ${LBL}
bash analyze_${LBL}.sh
cd ..
