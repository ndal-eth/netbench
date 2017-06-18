import numpy as np
import csv
import sys
import os


##################################
# Setup
#

print("NetBench python analysis tool v0.01")

# Usage print
def print_usage():
    print("Usage: python analyze.py /path/to/run/folder")

# Check length of arguments
if len(sys.argv) != 2:
    print("Number of arguments must be exactly two: analyze.py and /path/to/run/folder.")
    print_usage()
    exit()

# Check run folder path given as first argument
run_folder_path = sys.argv[1]
if not os.path.isdir(run_folder_path):
    print("The run folder path does not exist: " + run_folder_path)
    print_usage()
    exit()

# Create analysis folder
analysis_folder_path = run_folder_path + '/analysis_1s'
if not os.path.exists(analysis_folder_path):
    os.makedirs(analysis_folder_path)


##################################
# Analyze flow completion
#
def analyze_flow_completion():
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
            st = float(row[5])

            if st >= 500000000 and st < 1500000000:
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

        print("Calculating statistics...")

        statistics = {
            'general_num_flows': len(flow_ids),
            'general_num_unique_sources': len(set(source_ids)),
            'general_num_unique_targets': len(set(target_ids)),
            'general_flow_size_bytes_mean': np.mean(total_size_bytes),
            'general_flow_size_bytes_std': np.std(total_size_bytes)
        }

        range_low =                     [-1,            -1,            -1,              100000,     2434900,            1000000,    10000000]
        range_high =                    [-1,            100000,        2434900,         -1,         -1,                 -1,         -1]
        range_name =                    ["all",         "less_100KB",  "less_2.4349MB", "geq_100KB", "geq_2.4349MB",    "geq_1MB",  "geq_10MB"]
        range_completed_duration =      [[],            [],            [],              [],         [],                 [],         []]
        range_completed_throughput =    [[],            [],            [],              [],         [],                 [],         []]
        range_num_finished_flows =      [0,             0,             0,               0,          0,                  0,          0]
        range_num_unfinished_flows =    [0,             0,             0,               0,          0,                  0,          0]
        range_low_eq =                  [0,             0,             0,               1,          1,                  1,          1,]
        range_high_eq =                 [0,             0,             0,               1,          1,                  1,          1,]


        # Go over all flows
        for i in range(0, len(flow_ids)):

            # Range-specific
            for j in range(0, len(range_name)):
                if (
                        (range_low[j] == -1 or (range_low_eq[j] == 0 and total_size_bytes[i] > range_low[j]) or (range_low_eq[j] == 1 and total_size_bytes[i] >= range_low[j])) and
                        (range_high[j] == -1 or (range_high_eq[j] == 0 and total_size_bytes[i] < range_high[j]) or (range_high_eq[j] == 1 and total_size_bytes[i] <= range_high[j]))
                ):
                    if completed[i]:
                        range_num_finished_flows[j] += 1
                        range_completed_duration[j].append(duration[i])
                        range_completed_throughput[j].append(total_size_bytes[i] * 8 / duration[i])

                    else:
                        range_num_unfinished_flows[j] += 1

        # Ranges statistics
        for j in range(0, len(range_name)):

            # Number of finished flows
            statistics[range_name[j] + '_num_flows'] = range_num_finished_flows[j] + range_num_unfinished_flows[j]
            statistics[range_name[j] + '_num_finished_flows'] = range_num_finished_flows[j]
            statistics[range_name[j] + '_num_unfinished_flows'] = range_num_unfinished_flows[j]
            total = (range_num_finished_flows[j] + range_num_unfinished_flows[j])
            if range_num_finished_flows[j] != 0:
                statistics[range_name[j] + '_flows_completed_fraction'] = float(range_num_finished_flows[j]) / float(total)
                statistics[range_name[j] + '_mean_fct_ns'] = np.mean(range_completed_duration[j])
                statistics[range_name[j] + '_median_fct_ns'] = np.median(range_completed_duration[j])
                statistics[range_name[j] + '_99th_fct_ns'] = np.percentile(range_completed_duration[j], 99)
                statistics[range_name[j] + '_99.9th_fct_ns'] = np.percentile(range_completed_duration[j], 99.9)
                statistics[range_name[j] + '_mean_fct_ms'] = statistics[range_name[j] + '_mean_fct_ns'] / 1000000
                statistics[range_name[j] + '_median_fct_ms'] = statistics[range_name[j] + '_median_fct_ns'] / 1000000
                statistics[range_name[j] + '_99th_fct_ms'] = statistics[range_name[j] + '_99th_fct_ns'] / 1000000
                statistics[range_name[j] + '_99.9th_fct_ms'] = statistics[range_name[j] + '_99.9th_fct_ns'] / 1000000
                statistics[range_name[j] + '_throughput_mean_Gbps'] = np.mean(range_completed_throughput[j])
                statistics[range_name[j] + '_throughput_median_Gbps'] = np.median(range_completed_throughput[j])
                statistics[range_name[j] + '_throughput_99th_Gbps'] = np.percentile(range_completed_throughput[j], 99)
                statistics[range_name[j] + '_throughput_99.9th_Gbps'] = np.percentile(range_completed_throughput[j], 99.9)
                statistics[range_name[j] + '_throughput_1th_Gbps'] = np.percentile(range_completed_throughput[j], 1)
                statistics[range_name[j] + '_throughput_0.1th_Gbps'] = np.percentile(range_completed_throughput[j], 0.1)
            else:
                statistics[range_name[j] + '_flows_completed_fraction'] = 0

        # Print raw results
        print('Writing to result file flow_completion.statistics...')
        with open(analysis_folder_path + '/flow_completion.statistics', 'w+') as outfile:
            for key, value in sorted(statistics.items()):
                outfile.write(str(key) + "=" + str(value) + "\n")


##################################
# Analyze port utilization
#
def analyze_port_utilization():
    with open(run_folder_path + '/port_utilization.csv.log') as file:
        reader = csv.reader(file)

        # Column lists
        source_ids = []
        target_ids = []
        attached_to_server = []
        utilized_ns = []
        utilization = []
        utilization_server_ports = []
        utilization_non_server_ports = []
        num_server_port_zero = 0
        num_non_server_port_zero = 0

        print("Reading in port utilization log file...")

        # Read in column lists
        for row in reader:
            source_ids.append(float(row[0]))
            target_ids.append(float(row[1]))
            attached_to_server.append(row[2] == 'Y')
            utilized_ns.append(float(row[3]))
            utilization.append(float(row[4]))
            if row[2] == 'Y':
                utilization_server_ports.append(float(row[4]))
                if float(row[4]) == 0:
                    num_server_port_zero += 1
            else:
                utilization_non_server_ports.append(float(row[4]))
                if float(row[4]) == 0:
                    num_non_server_port_zero += 1

            if len(row) != 5:
                print("Invalid row: ", row)
                exit()

        print("Calculating statistics...")

        # General statistics (there is always a server port)
        statistics = {

            'all_port_num': len(source_ids),
            'all_port_unique_sources': len(set(source_ids)),
            'all_port_unique_targets': len(set(target_ids)),
            'all_port_mean_utilization': np.mean(utilization),
            'all_port_median_utilization': np.median(utilization),
            'all_port_std_utilization': np.std(utilization),
            'all_port_99th_utilization': np.percentile(utilization, 99),
            'all_port_99.9th_utilization': np.percentile(utilization, 99.9),

            'server_port_num': len(utilization_server_ports),
            'server_port_zero_num': num_server_port_zero,
            'server_port_mean_utilization': np.mean(utilization_server_ports),
            'server_port_median_utilization': np.median(utilization_server_ports),
            'server_port_std_utilization': np.std(utilization_server_ports),
            'server_port_99th_utilization': np.percentile(utilization_server_ports, 99),
            'server_port_99.9th_utilization': np.percentile(utilization_server_ports, 99.9)

        }

        # Only print non-server port statistics if they exist
        statistics['non_server_port_num'] = len(utilization_non_server_ports)
        if len(utilization_non_server_ports) > 0:
            statistics['non_server_ports_zero_num'] = num_non_server_port_zero
            statistics['non_server_port_mean_utilization'] = np.mean(utilization_non_server_ports)
            statistics['non_server_port_median_utilization'] = np.median(utilization_non_server_ports)
            statistics['non_server_port_std_utilization'] = np.std(utilization_non_server_ports)
            statistics['non_server_port_99th_utilization'] = np.percentile(utilization_non_server_ports, 99)
            statistics['non_server_port_99.9th_utilization'] = np.percentile(utilization_non_server_ports, 99.9)

        # Print raw results
        print('Writing to result file port_utilization.statistics...')
        with open(analysis_folder_path + '/port_utilization.statistics', 'w+') as outfile:
            for key, value in sorted(statistics.items()):
                outfile.write(str(key) + "=" + str(value) + "\n")

# Call analysis functions
analyze_flow_completion()
analyze_port_utilization()
