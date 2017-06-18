package ch.ethz.systems.netbench.core.config;

public class BaseAllowedProperties {

    private BaseAllowedProperties() {
        // Private constructor, cannot be constructed
    }

    public static final String[] LOG = new String[]{
            "enable_log_port_queue_state",
            "enable_log_flow_throughput",
            "enable_generate_human_readable_flow_completion_log",
    };

    public static final String[] PROPERTIES_RUN = new String[] {

            // General
            "scenario_topology_file",
            "scenario_topology_extend_with_servers",
            "scenario_topology_extend_servers_per_tl_node",
            "seed",
            "run_time_s",
            "run_time_ns",
            "run_folder_name",
            "run_folder_base_dir",
            "analysis_command",
            "finish_when_first_flows_finish",

            // Infrastructure
            "transport_layer",
            "network_device",
            "network_device_intermediary",
            "output_port",
            "link",

            // Routing
            "network_device_routing",

            // Traffic
            "traffic",
            "traffic_flow_size_dist",
            "traffic_probabilities_file",
            "traffic_probabilities_generator",
            "traffic_probabilities_active_fraction",
            "traffic_probabilities_active_fraction_is_ordered",
            "traffic_lambda_flow_starts_per_s"


    };

    public static final String[] EXTENSION = new String[]{

            // Basic
            "output_port_max_queue_size_bytes",
            "output_port_ecn_threshold_k_bytes",
            "link_delay_ns",
            "link_bandwidth_bit_per_ns",

            // Poisson traffic
            "traffic_pair_type",
            "traffic_pair_flow_size_byte",
            "traffic_pairs",
            "traffic_arrivals_list",
            "traffic_flow_size_dist_uniform_mean_bytes",
            "traffic_flow_size_dist_pareto_shape",
            "traffic_flow_size_dist_pareto_mean_kilobytes",
            "traffic_pareto_skew_shape",

            // Flowlet
            "FLOWLET_GAP_NS",

            // VLB
            "routing_random_valiant_node_range_lower_incl",
            "routing_random_valiant_node_range_upper_incl",
            "routing_ecmp_then_valiant_switch_threshold_bytes",

            "traffic_probabilities_fraction_A",
            "traffic_probabilities_mass_A",


    };

    public static final String[] EXPERIMENTAL = new String[]{

            // TCP
            "TCP_ROUND_TRIP_TIMEOUT_NS",
            "TCP_MAX_SEGMENT_SIZE",
            "TCP_MAX_WINDOW_SIZE",
            "TCP_LOSS_WINDOW_SIZE",
            "TCP_INITIAL_SLOW_START_THRESHOLD",
            "TCP_INITIAL_WINDOW_SIZE",
            "TCP_MINIMUM_SSTHRESH",
            "TCP_MINIMUM_ROUND_TRIP_TIMEOUT",
            "DCTCP_WEIGHT_NEW_ESTIMATION",
            "enable_log_congestion_window",
            "enable_log_packet_burst_gap",

            // K-shortest-paths
            "k_for_k_shortest_paths",

            // K-paths
            // "k_paths_k_threshold",
            // "k_paths_file",
            "allow_source_routing_skip_duplicate_paths",
            "allow_source_routing_add_duplicate_paths",


            "spark_error_distribution",
            "routing_ecmp_then_source_routing_switch_threshold_bytes"

    };

}
