import os
import sys


# Usage print
def print_usage():
    print("USAGE: python multi_combine.py property_name property_file /path/to/collection/run/folder splitkeyword untilkeyword")
    print("E.g. : python multi_combine.py all_mean_fct_ms flow_completion.statistics ../temp/results/projector flows flows")

# Check number of arguments
if len(sys.argv) != 6:
    print("Insufficient number of arguments")
    print_usage()
    exit()

# Property
combine_property = sys.argv[1]

# Property file
combine_property_file = sys.argv[2]

# Check collection run folder path given as first argument
coll_run_folder_path = sys.argv[3]
if not os.path.isdir(coll_run_folder_path):
    print("ERROR: the collection run folder path does not exist: " + coll_run_folder_path)
    print_usage()
    exit()

# Split keyword
split_key_word = sys.argv[4]

# Split keyword
until_key_word = sys.argv[5]

# Name all the direct sub-folders
sub_folders = [ name for name in os.listdir(coll_run_folder_path) if os.path.isdir(os.path.join(coll_run_folder_path, name)) ]

# Call the analysis on each of the sub-folders (which are run folders)
for f in sub_folders:
    run_folder_path = coll_run_folder_path + "/" + f

    f_parts = f.split("_")

    f_group = ""
    for i in range(0, len(f_parts)):
        if f_parts[i] == until_key_word:
            break
        f_group = f_group + "_" + f_parts[i]

    f_key = ""
    for i in range(0, len(f_parts)):
        if f_parts[i] == split_key_word:
            f_key = f_parts[i + 1]
            break


    # File
    full_file = run_folder_path + '/analysis_1s/' + combine_property_file
    if not os.path.isfile(full_file):
        print("ERROR: file " + full_file + " does not exist")
        exit()

    # Open the desired file
    found = False
    with open(full_file) as file:

        # Read in column lists
        for line in file:
            key_value = line.split("=")
            key = key_value[0]
            value = key_value[1].strip()

            if key == combine_property:
                print(f_group[1:] + "\t" + f_key + "\t" + value)
                found = True

    # Give warning if the property is not found
    if not found:
        print("ERROR: did not find combine property in run folder " + f)
        exit()
