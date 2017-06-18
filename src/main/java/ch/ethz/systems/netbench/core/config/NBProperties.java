package ch.ethz.systems.netbench.core.config;

import ch.ethz.systems.netbench.core.config.exceptions.ConfigurationReadFailException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyMissingException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyNotExistingException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import edu.asu.emit.algorithm.graph.Graph;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * NetBench properties manager.
 *
 * Manages all the properties defined in the NetBench properties
 * configuration file. Exits the program if properties are defined
 * that do not exist in the NetBench system.
 */
public class NBProperties extends Properties {

    // Set of allowed properties
    private final Set<String> allowedProperties;

    // Master run properties file (not the default)
    private final String fileName;

    // Cache properties variables
    private boolean graphIsRead;
    private Graph graph;
    private GraphDetails graphDetails;

    /**
     * Create property manager without anything set.
     *
     * @param additionalAllowedProperties   Additional allowed properties
     */
    public NBProperties(String[]... additionalAllowedProperties) {
        this.fileName = null;

        // Cached properties variables
        this.graphIsRead = false;
        this.graph = null;
        this.graphDetails = null;
        this.allowedProperties = new HashSet<>();
        initializeAllowedProperties(additionalAllowedProperties);

        // Make sure all properties are legal
        this.checkProperties();

    }

    /**
     * Create property manager solely from a file.
     *
     * @param fileName  File name (e.g. C:/example/run.properties)
     * @param additionalAllowedProperties   Additional allowed properties
     */
    public NBProperties(String fileName, String[]... additionalAllowedProperties) {
        this.fileName = fileName;

        // Read completely from file
        try {
            InputStream input = new FileInputStream(fileName);
            this.load(input);
            input.close();
        } catch (IOException ex) {
            throw new ConfigurationReadFailException(this, ex);
        }

        // Cached properties variables
        this.graphIsRead = false;
        this.graph = null;
        this.graphDetails = null;
        this.allowedProperties = new HashSet<>();
        initializeAllowedProperties(additionalAllowedProperties);

        // Make sure all properties are legal
        this.checkProperties();

    }

    /**
     * Initialize all allowed properties.
     *
     * @param additionalAllowedProperties   Additional allowed properties arrays
     */
    private void initializeAllowedProperties(String[]... additionalAllowedProperties) {

        // Additional properties
        int totalAdded = 0;
        for (String[] addProperties : additionalAllowedProperties) {
            allowedProperties.addAll(Arrays.asList(addProperties));
            totalAdded += addProperties.length;
        }

        // Check for no duplicates
        if (allowedProperties.size() != totalAdded) {
            throw new IllegalArgumentException("Cannot allow a property twice, " +
                    "there is a duplicate between the arrays of allowed properties.");
        }

    }

    /**
     * Check that all properties are indeed allowed.
     * Does not check values.
     *
     * Throws a <i>PropertyNotExistingException</i> if
     * a property defined does not exist in the NetBench system.
     */
    private void checkProperties() {

        // If it was not found among them, it is an illegal property name
        for (Object keyObj : this.keySet()) {
            checkIsAllowedProperty((String) keyObj);
        }

    }

    /**
     * Check whether a key is a valid property.
     * Throws an exception if not.
     *
     * @param key   Key
     */
    private void checkIsAllowedProperty(String key) {
        for (String s : allowedProperties) {
            if (key.equals(s)) {
                return;
            }
        }
        throw new PropertyNotExistingException(this, key);
    }

    /**
     * Retrieve the property value as a string or throw
     * a runtime exception if the property does not exist.
     *
     * @param key   Property key
     *
     * @return Property value
     */
    public String getPropertyOrFail(String key) {
        checkIsAllowedProperty(key);
        String result = this.getProperty(key);
        if (result == null) {
            throw new PropertyMissingException(this, key);
        } else {
            return result;
        }
    }

    /**
     * Check whether a property is defined.
     *
     * @param key   Property key
     *
     * @return True iff a property with the given key is defined (not null)
     */
    public boolean isPropertyDefined(String key) {
        checkIsAllowedProperty(key);
        return this.getProperty(key) != null;
    }

    /**
     * Retrieve the property value as an integer or throw
     * a runtime exception if the property does not exist.
     * Can throw number format exception if it fails to convert
     * the value to an integer.
     *
     * @param key   Property key
     *
     * @return Property integer value
     */
    public int getIntegerPropertyOrFail(String key) {
        return Integer.valueOf(getPropertyOrFail(key));
    }

    /**
     * Retrieve the property value as an boolean or throw
     * a runtime exception if the property does not exist.
     * Can run time exception if it fails to convert
     * the value to a boolean.
     *
     * @param key   Property key
     *
     * @return Property boolean value
     */
    public boolean getBooleanPropertyOrFail(String key) {
        String foundValue = this.getPropertyOrFail(key);
        switch (foundValue) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new PropertyValueInvalidException(this, key);
        }
    }

    /**
     * Retrieve the property value as a long or throw
     * a runtime exception if the property does not exist.
     * Can throw number format exception if it fails to convert
     * the value to a long.
     *
     * @param key   Property key
     *
     * @return Property long value
     */
    public long getLongPropertyOrFail(String key) {
        return Long.valueOf(getPropertyOrFail(key));
    }

    /**
     * Retrieve the property value as a double or throw
     * a runtime exception if the property does not exist.
     * Can throw number format exception if it fails to convert
     * the value to a double.
     *
     * @param key   Property key
     *
     * @return Property double value
     */
    public double getDoublePropertyOrFail(String key) {
        return Double.valueOf(getPropertyOrFail(key));
    }

    /**
     * Retrieve a directed pair list.
     *
     * @param key   Property key
     *
     * @return  Even-sized list where even are the left side, uneven are the right side,
     *          as such "a->3,c->d" returns "[a, 3, c, d]
     *
     */
    public List<Integer> getDirectedPairsListPropertyOrFail(String key) {
        String list = getPropertyOrFail(key);
        List<Integer> integerList = new ArrayList<>();
        for (String s : list.split(",")) {
            for (String s2 : s.split("->")) {
                integerList.add(Integer.valueOf(s2));
            }
        }
        return integerList;
    }

    /**
     * Retrieve primary file used for the NBProperties (does not include the default file).
     *
     * @return  File name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Retrieve the topology (properties key: scenario_topology_file) from file
     *
     * @return  Graph instance of the topology
     */
    public Graph getGraph() {
        if (!graphIsRead) {
            readGraph();
        }
        return graph;
    }

    /**
     * Retrieve the graph details from file (properties key: scenario_topology_file).
     *
     * @return  Graph instance of the topology
     */
    public GraphDetails getGraphDetails() {
        if (!graphIsRead) {
            readGraph();
        }
        return graphDetails;
    }

    /**
     * Read in the graph and its details.
     */
    private void readGraph() {
        Pair<Graph, GraphDetails> result = GraphReader.read(this.getPropertyOrFail("scenario_topology_file"));
        graph = result.getLeft();
        graphDetails = result.getRight();
        graphIsRead = true;
    }

    /**
     * Retrieve the topology file name (properties key: scenario_topology_file).
     *
     * @return  Topology file name
     */
    public String getTopologyFileNameOrFail() {
        return this.getPropertyOrFail("scenario_topology_file");
    }

    /**
     * Retrieve a boolean property with a default if it is not found.
     * Will fail if the property value string is anything other than "true" or "false".
     *
     * @param key           Property key
     * @param defaultValue  Default boolean value
     *
     * @return  Boolean property value
     */
    public boolean getBooleanPropertyWithDefault(String key, boolean defaultValue) {
        checkIsAllowedProperty(key);
        String foundValue = this.getProperty(key);
        if (foundValue != null) {
            switch (foundValue) {
                case "true":
                    return true;
                case "false":
                    return false;
                default:
                    throw new PropertyValueInvalidException(this, key);
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * Retrieve a long property with a default if it is not found.
     *
     * @param key               Property key
     * @param defaultValue      Default long value
     *
     * @return  Long property value
     */
    public long getLongPropertyWithDefault(String key, long defaultValue) {
        checkIsAllowedProperty(key);
        String foundValue = this.getProperty(key);
        if (foundValue != null) {
            return Long.valueOf(foundValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Retrieve a double property with a default if it is not found.
     *
     * @param key               Property key
     * @param defaultValue      Default double value
     *
     * @return  Double property value
     */
    public double getDoublePropertyWithDefault(String key, double defaultValue) {
        checkIsAllowedProperty(key);
        String foundValue = this.getProperty(key);
        if (foundValue != null) {
            return Double.valueOf(foundValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Retrieve a integer property with a default if it is not found.
     *
     * @param key               Property key
     * @param defaultValue      Default integer value
     *
     * @return  Integer property value
     */
    public int getIntegerPropertyWithDefault(String key, int defaultValue) {
        checkIsAllowedProperty(key);
        String foundValue = this.getProperty(key);
        if (foundValue != null) {
            return Integer.valueOf(foundValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Retrieve a (string) property with a default if it is not found.
     *
     * @param key           Property key
     * @param defaultValue  Default string value
     *
     * @return  String property value
     */
    public String getPropertyWithDefault(String key, String defaultValue) {
        checkIsAllowedProperty(key);
        String foundValue = this.getProperty(key);
        if (foundValue != null) {
            return foundValue;
        } else {
            return defaultValue;
        }
    }

    /**
     * Override a property.
     *
     * @param key               Property key
     * @param overrideValue     Value to override with
     */
    public void overrideProperty(String key, String overrideValue) {
        checkIsAllowedProperty(key);

        // Display override message
        if (!isPropertyDefined(key)) {
            System.out.println("WARNING: property " + key + " which is being overridden is not previously defined in the configuration.");
         }
        System.out.println("Overriding property " + key + ": \"" + getPropertyWithDefault(key, "NON-EXISTING") + "\" (old) -> \"" + overrideValue + "\" (new).");

        // Actually perform override
        this.setProperty(key, overrideValue);

    }

    /**
     * Get all the properties and their values as a string.
     *
     * @return  Properties and their values as a string (new line separated)
     */
    public String getAllPropertiesToString() {
        StringBuilder res = new StringBuilder();
        for (Object keyObj : this.keySet()) {
            res.append(keyObj.toString());
            res.append(": \"");
            res.append(this.getProperty((String) keyObj));
            res.append("\"\n");
        }
        return res.toString();
    }

}
