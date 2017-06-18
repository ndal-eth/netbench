import numpy as np
import csv
import sys
import os


##################################
# Setup
#

print("NetBench python CreateCDF tool v0.01")

# Usage print
def print_usage():
    print("Usage: python create_cdf.py /path/to/run/folder")

# Check length of arguments
if len(sys.argv) != 2:
    print("Number of arguments must be exactly two: create_cdf.py and /path/to/run/folder.")
    print_usage()
    exit()

# Check run folder path given as first argument
run_folder_path = sys.argv[1]
if not os.path.isdir(run_folder_path):
    print("The run folder path does not exist: " + run_folder_path)
    print_usage()
    exit()

# Create analysis folder
analysis_folder_path = run_folder_path + '/analysis'
if not os.path.exists(analysis_folder_path):
    os.makedirs(analysis_folder_path)

# CDF interpolation x-values
plot_x_vals = []
plot_x_vals.extend(np.arange(1e-8, 1e-7, 1e-10))
plot_x_vals.extend(np.arange(1e-7, 1e-6, 1e-9))
plot_x_vals.extend(np.arange(1e-6, 1e-5, 1e-8))
plot_x_vals.extend(np.arange(1e-5, 1e-4, 1e-7))
plot_x_vals.extend(np.arange(1e-4, 1e-3, 1e-6))
plot_x_vals.extend(np.arange(1e-3, 1e-2, 1e-5))
plot_x_vals.extend(np.arange(1e-2, 1e-1, 1e-4))
plot_x_vals.extend(np.arange(1e-1, 1e0, 1e-3))
plot_x_vals.extend(np.arange(1e0, 1e1, 1e-2))
plot_x_vals.extend(np.arange(1e1, 1e2, 1e-1))
plot_x_vals.extend(np.arange(1e2, 1e3, 1e0))
plot_x_vals.extend(np.arange(1e3, 1e4, 1e1))
plot_x_vals.extend(np.arange(1e4, 1e5, 1e2))
plot_x_vals.extend(np.arange(1e5, 1e6, 1e3))
plot_x_vals.extend(np.arange(1e6, 1e7, 1e4))
plot_x_vals.extend(np.arange(1e7, 1e8, 1e5))
plot_x_vals.extend(np.arange(1e8, 1e9, 1e6))
plot_x_vals.extend(np.arange(1e9, 1e10, 1e7))
plot_x_vals.extend(np.arange(1e10, 1e11, 1e8))
plot_x_vals.extend(np.arange(1e11, 1e12, 1e9))
plot_x_vals.extend(np.arange(1e12, 1e13, 1e10))

##################################
# Analyze flow completion
#
def analyze_flow_completion_cdfs():
    with open(run_folder_path + '/flow_completion.csv.log') as file:
        reader = csv.reader(file)

        # To enable preliminary read to determine size:
        # data = list(reader)
        # row_count = len(data)

        # Column lists
        flow_ids = []
        source_ids = []
        target_ids = []
        sent_bytes = []
        total_size_bytes = []
        start_time = []
        end_time = []
        duration = []
        completed = []

        print("Reading in flow completion log file...")

        # Read in column lists
        for row in reader:
            flow_ids.append(float(row[0]))
            source_ids.append(float(row[1]))
            target_ids.append(float(row[2]))
            sent_bytes.append(float(row[3]))
            total_size_bytes.append(float(row[4]))
            start_time.append(float(row[5]))
            end_time.append(float(row[6]))
            duration.append(float(row[7]))
            completed.append(row[8] == 'TRUE')
            if len(row) != 9:
                print("Invalid row: ", row)
                exit()

        range_low =                     [-1,            -1,            1000000,    10000000]
        range_high =                    [-1,            100000,        -1,         -1]
        range_name =                    ["all",         "leq_100KB",   "geq_1MB",  "geq_10MB"]
        range_completed_duration =      [[],            [],            [],         []]

        # Go over all flows
        for i in range(0, len(flow_ids)):

            # Range-specific
            for j in range(0, len(range_name)):
                if (
                            (total_size_bytes[i] >= range_low[j] or range_low[j] == -1) and
                            (total_size_bytes[i] <= range_high[j] or range_high[j] == -1)
                ):
                    if completed[i]:
                        range_completed_duration[j].append(duration[i])

        # Ranges statistics
        for j in range(0, len(range_name)):

            # Create CDF
            sorted_data = np.sort(range_completed_duration[j])
            cdf_y_vals = np.arange(len(sorted_data))/float(len(sorted_data)-1)

            plot_y_vals = np.interp(plot_x_vals, sorted_data, cdf_y_vals)

            # Write CDF of range to file
            print('Writing to result file cdf_fct_' + range_name[j] + '.cdf...')
            with open(analysis_folder_path + '/cdf_fct_' + range_name[j] + ".cdf", 'w+') as outfile:
                for k in range(0, len(plot_x_vals)):
                    if k == len(plot_x_vals)-1 or plot_y_vals[k + 1] != 0:
                        outfile.write(str(plot_x_vals[k]) + "\t" + str(plot_y_vals[k]) + "\n")
                    if plot_y_vals[k] == 1 and plot_y_vals[k + 1] == 1:
                        break

analyze_flow_completion_cdfs()