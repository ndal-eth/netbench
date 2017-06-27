###
###  Released under the MIT License (MIT) --- see ../LICENSE
###  Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla, Simon Kassing
###

# Note you need gnuplot 4.4 for the pdfcairo terminal.
set terminal pdfcairo font "Helvetica, 16" linewidth 1.5 rounded dashed

# Line style for axes
set style line 80 lt rgb "#808080"

# Line style for grid
set style line 81 lt 0  # dashed
set style line 81 lt rgb "#cccccc"  # grey

set object 1 rect from 0.0001,0 to 100000,1 fc "red" fs transparent solid 0.05 noborder

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
set style line 1 lt rgb "#2177b0" lw 3 pt 6 ps 0
set style line 2 lt rgb "#fc7f2b" lw 3 pt 1 ps 0
set style line 3 lt rgb "#2f9e37" lw 2 pt 2 ps 1.4
set style line 4 lt rgb "#d42a2d" lw 2 pt 8 ps 1.3
set style line 5 lt rgb "#9167b8" lw 1.6 pt 3 ps 1
set style line 6 lt rgb "#8a554c" lw 1.6 pt 7
set style line 7 lt rgb "#e079be" lw 1.6 pt 6 ps 1
set style line 8 lt rgb "#7d7d7d" lw 1.6 pt 7 ps 1
set style line 9 lt rgb "#666666" lw 1.2 pt 0

#-----
#set object circle at first 30000,0.5 radius char 0.5 \
 #   fillstyle empty border lc rgb '#aa1100' lw 2

set label at 100000, 0.96, 0 "Mean = 100KB" point pointtype 7 lc rgb "#fc7f2b" pointsize 1 offset -1.4,-1.1 font ",17"
set label at 2434900, 0.822, 0 "Mean = 2.4MB" point pointtype 7 lc rgb "#2177b0" pointsize 1 offset 0.7,-0.5 font ",17"
set label at 20000, 0.14, 0 "Short" textcolor "#CC0000" font ",17"
set label at 130000, 0.14, 0 "Long" textcolor "#777777" font ",17"

set log x
set output "output_2b_flow_size.pdf"
set format x "10^{%T}"
set xlabel "Flow size (bytes)"
set ylabel "CDF"

set xrange [1e3:1e9]
set yrange [0:1]
set key font ",16"
set key bottom right Left reverse
#set key below Left reverse
#set key tmargin
#set key invert
set key height 4.3
set key nobox

#
plot    "fs_pareto_s_1.05_mu_100KB_cdf.txt" using 1:2 title "Pareto-HULL" smooth unique w lp ls 2, \
        "fs_pfabric_web_search_cdf.txt" using 1:2 title "pFabric Web search" smooth unique w lp ls 1, \




