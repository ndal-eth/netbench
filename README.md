![NetBench](images/netbench_banner.png)

NetBench is a packet-level simulator, focused on modelling congestion and queueing.

## Getting Started

#### 1. Software dependencies

* **Java 8:** Version 8 of Java; both Oracle JDK and OpenJDK are supported and produce under that same seed deterministic results. Additionally the project uses the Apache Maven software project management and comprehension tool (version 3+).

* **Python 2 (optional):** Recent version of Python 2 for analysis; be sure you can globally run `python <script.py>` (see step 2.2).

#### 2. Building

1. Compile and run all tests in the project, make sure that they all pass; this can be done using the following maven command: `mvn compile test`

2. Build the executable `NetBench.jar` by using the following maven command: `mvn clean compile assembly:single`

#### 3. Running

1. Execute a demo run by using the following command: `java -jar -ea NetBench.jar ./example/runs/demo.properties`

2. After the run, the log files are saved in the `./temp/demo` folder

3. If you have python 2 installed, you can view calculated statistics about flow completion and port utilization (e.g. mean FCT, 99th %-tile port utilization, ....) in the `./temp/demo/analysis` folder.

## Software structure

There are three sub-packages in *netbench*: (a) core, containing core functionality, (b) ext (extension), which contains functionality implemented and quite thoroughly tested, and (c) xpt (experimental), which contains functionality not yet as thoroughly tested but reasonably vetted and assumed to be correct for the usecase it was written for.

The framework is written based on five core components:
1. **Network device**: abstraction of a node, can be a server (has a transport layer) or merely function as switch (no transport layer);
2. **Transport layer**: maintains the sockets for each of the flows that are started at the network device and for which it is the destination;
3. **Intermediary**: placed between the network device and transport layer, is able to modify each packet before arriving at the transport layer and after leaving the transport layer;
4. **Link**: quantifies the capabilities of the physical link, which the output port respects;
5. **Output port**: models output ports and their queueing behavior.

Look into `ch.ethz.systems.netbench.ext.demo` for an impression how to extend the framework.  If you've written an extension, it is necessary to add it in its respective selector in `ch.ethz.systems.netbench.run`. If you've added new properties, be sure to add them in the `ch.ethz.systems.netbench.config.BaseAllowedProperties` class.

More information about the framework can be found in the thesis located at [https://www.research-collection.ethz.ch/handle/20.500.11850/156350](https://www.research-collection.ethz.ch/handle/20.500.11850/156350) (section 4.2: NetBench: Discrete Packet Simulator).

## Reproducing "Beyond fat-trees without antennae, mirrors, and disco-balls"

The packet simulations in the paper "Beyond fat-trees without antennae, mirrors, and disco-balls" (SIGCOMM 2017), have been run using the first commit of this repository.

#### General remarks about structure

1. Make sure you understand and ran through the Getting Started section above;

2. All paper-specific files are placed within the `./private` folder, which would normally be excluded from the *netbench* repository (with the aim of modularization). Similarly, the `./paths-cache` would normally be excluded from git, as caches can become significantly large;

3. All run configurations are placed with the `./private/runs` folder, in their respective sub-folders (e.g. `./private/runs/1_failcases`, `./private/runs/9_oversub`);

4. The output of the runs are written to the `./temp/results` folder, in their respective output sub-folders (e.g. `./temp/results/1_failcases`, `./temp/results/9_oversub`, ...);

5. Directly in the `./private` folder is *analyze_all.sh*, which automatically goes over all the generated results in `./temp/results` and creates in each run folder the `analysis_1s` folder, which contains statistics later used in the plot aggregation;

6. Plotting can be done by executing the *plot_all.sh* command in `./private/plots` folder (which calls sub-plot commands, see file for details).

#### Replotting without rerunning

For convenience, we've added all the raw results without the logs (which are around 128GB, too big for git) in the `./temp/results` folder. As such, you can view these aggregate statistics and create your own plots. As mentioned above, there is in `./private/plots` a file called `plot_all.sh` which will regenerate **all** packet simulation plots shown in the paper. Output of gnuplot differs slightly based on OS (there might be some unwanted text overlap). The plots are viewable in the `private/plots` sub-folders, e.g. `private/plots/9_oversub/output_9a_oversub_avg_fct.pdf`. The plots are already available as well.

#### Run example (1_failcases)

Let's now go for an example, wanting to reproduce the `1_failcases` experiments.

1. Look into folder `./private/runs/1_failcases`, it contains the various `*.property` run configuration files and the `deploy_1_failcases.sh` script;
2. Edit the `deploy_1_failcases.sh` folder, such that each command (e.g. `ssh user@machine.com "cd /path/to/folder/netbench; screen -d -m ../java/jre1.8.0_131/bin/java -ea -jar NetBench.jar [REST...]"`) is executed properly. It is important that the command is executed from the *netbench* folder (done via the cd ... part of the command in my setup);
3. Execute `deploy_1_failcases.sh` to run the experiment (again, check everything is running OK, regular debugging practice is to manuallly try out one);
4. The results of the run are written to `./temp/results/1_failcases` folder. If your setup does not write everything to a drive shared by all machines, you will need to download them separately from each machine into the `./temp/results/1_failcases` folder on the machine where you do the analysis/plotting;
5. Go to the folder `./private/analysis` and execute command `python multi_analyze_1s.py ../temp/results/1_failcases`, this will create the statistics folders in each of the run folders in `./temp/results/1_failcases`;
6. Go to the folder `./private/plots/1_failcases` and execute `./analyze_1_failcases.sh`;
7. The plots are then shown in folder `./private/plots/1_failcases`, e.g. `output_1a_failcase_ecmp_avg_fct.pdf`.
