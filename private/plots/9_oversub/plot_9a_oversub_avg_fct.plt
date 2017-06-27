###
###  Released under the MIT License (MIT) --- see ../LICENSE
###  Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla, Simon Kassing
###

# Note you need gnuplot 4.4 for the pdfcairo terminal.
set terminal pdfcairo font "Helvetica, 18" linewidth 1.5 rounded dashed

# Line style for axes
set style line 80 lt rgb "#808080"

# Line style for grid
set style line 81 lt 0  # dashed
set style line 81 lt rgb "#cccccc"  # grey

set grid back linestyle 81
set border 3 back linestyle 80 # Remove border on top and right.  These
             # borders are useless and make it harder
	                  # to see plotted lines near the border.
			      # Also, put it in grey; no need for so much emphasis on a border.
			      set xtics nomirror
			      set ytics nomirror

#set log x
#set mxtics 10    # Makes logscale look good.

# Line styles: try to pick pleasing colors, rather
# than strictly primary colors or hard-to-see colors
# like gnuplot's default yellow.  Make the lines thick
# so they're easy to see in small plots in papers.
set style line 1 lt rgb "#2177b0" lw 2 pt 6 ps 1.4
set style line 2 lt rgb "#fc7f2b" lw 3 pt 1 ps 1
set style line 3 lt rgb "#2f9e37" lw 2 pt 2 ps 1.4
set style line 4 lt rgb "#d42a2d" lw 2 pt 8 ps 1.3
set style line 5 lt rgb "#9167b8" lw 1.6 pt 3 ps 1
set style line 6 lt rgb "#8a554c" lw 1.6 pt 7
set style line 7 lt rgb "#e079be" lw 1.6 pt 6 ps 1
set style line 8 lt rgb "#7d7d7d" lw 1.6 pt 7 ps 1
set style line 9 lt rgb "#666666" lw 1.2 pt 0

#-----

set output "output_9a_oversub_avg_fct.pdf"
set format x "%.0fK"
set xlabel "Load {/Symbol l} (flow-starts per second)"
set ylabel "Average FCT (ms)"

set xrange [0:120]

set yrange [0:]
set key font ",16"
set key height -0.5
set key top left Left reverse
#set key below Left reverse
#set key tmargin

plot    "data_9_oversub_mean_fct_ms.txt" using ($2/1000):(stringcolumn(1) eq "full_fat_tree_k16" ? $3: 1/0) title "Fat-tree" smooth unique w lp ls 1, \
        "data_9_oversub_mean_fct_ms.txt" using ($2/1000):(stringcolumn(1) eq "xpander_n216_d11_ecmp" ? $3: 1/0) title "Xpander ECMP" smooth unique w lp ls 2, \
        "data_9_oversub_mean_fct_ms.txt" using ($2/1000):(stringcolumn(1) eq "xpander_n216_d11_hybrid" ? $3: 1/0) title "Xpander HYB" smooth unique w lp ls 3, \
        "data_9_oversub_mean_fct_ms.txt" using ($2/1000):(stringcolumn(1) eq "full_fat_tree_k14" ? $3: 1/0) title "77%-Fat-tree" smooth unique w lp ls 6, \
        #"data_9_oversub_mean_fct_ms.txt" using ($2/1000):(stringcolumn(1) eq "xpander_n216_d11_vlb" ? $3: 1/0) title "Xpander VLB" smooth unique w lp ls 4, \
        #"data_9_oversub_mean_fct_ms.txt" using ($2/1000):(stringcolumn(1) eq "xpander_n216_d11_ksp_8" ? $3: 1/0) title "Xpander KSP8" smooth unique w lp ls 4, \
        #"data_9_oversub_mean_fct_ms.txt" using ($2/1000):(stringcolumn(1) eq "xpander_n216_d11_hybrid_ksp_8" ? $3: 1/0) title "Xpander HYB-KSP" smooth unique w lp ls 6, \
        #"data_9_oversub_mean_fct_ms.txt" using ($2/1000):(stringcolumn(1) eq "xpander_n216_d11_ksp_32" ? $3: 1/0) title "Xpander KSP32" smooth unique w lp ls 7, \
        #"data_9_oversub_mean_fct_ms.txt" using ($2/1000):(stringcolumn(1) eq "xpander_n216_d11_hybrid_ksp_32" ? $3: 1/0) title "Xpander HYB-KSP32" smooth unique w lp ls 8, \


