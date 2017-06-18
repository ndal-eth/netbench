import os
import sys
from subprocess import call


# Usage print
def print_usage():
    print("Usage: python multi_analyze.py /path/to/collection/run/folder")

# Check collection run folder path given as first argument
coll_run_folder_path = sys.argv[1]
if not os.path.isdir(coll_run_folder_path):
    print("The collection run folder path does not exist: " + coll_run_folder_path)
    print_usage()
    exit()

# Name all the direct sub-folders
sub_folders = [name for name in os.listdir(coll_run_folder_path) if os.path.isdir(os.path.join(coll_run_folder_path, name))]

# Call the analysis on each of the sub-folders (which are run folders)
i=1
for f in sub_folders:
    print(str(i) + "/" + str(len(sub_folders)) + "...")
    run_folder_path = coll_run_folder_path + "/" + f
    call(["python", "analyze.py", run_folder_path])
    i = i + 1
