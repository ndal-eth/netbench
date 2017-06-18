package ch.ethz.systems.netbench.core.config;

import ch.ethz.systems.netbench.core.config.exceptions.ConfigurationReadFailException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyMissingException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyNotExistingException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class NBPropertiesTest {

    private static final String[] TEST_ALLOWED_PROPERTIES = new String[]{
            "abc",
            "xyz",
            "ZZZ",
            "zzz",
            "Xyz",
            "643",
            "key",
            "bb",
            "cc",
            "scenario_topology_file"
    };

    @Test
    public void testDuplicateAllowedProp() throws IOException {

        // Create temporary files
        File tempConfig = File.createTempFile("temp-run-config", ".tmp");

        // Write temporary config file
        BufferedWriter configWriter = new BufferedWriter(new FileWriter(tempConfig));
        configWriter.write("abc=5");
        configWriter.close();

        // Create with a duplicate between the two sets
        boolean thrown = false;
        try {
            new NBProperties(tempConfig.getAbsolutePath(),
                    new String[]{"abc", "def"},
                    new String[]{"def", "xyz"}
            );
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Clean-up
        assertTrue(tempConfig.delete());

    }

    @Test
    public void testInvalidFile() throws IOException {

        // Create and then immediately file to get invalid file name
        File tempConfig = File.createTempFile("temp-run-config", ".tmp");
        assertTrue(tempConfig.delete());

        // Create with a duplicate between the two sets
        boolean thrown = false;
        try {
            new NBProperties(tempConfig.getAbsolutePath(),
                    new String[]{"abc", "def"},
                    new String[]{"deF", "xyz"}
            );
        } catch (ConfigurationReadFailException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testInvalidPropertyDefined() throws IOException {

        // Create temporary files
        File tempConfig = File.createTempFile("temp-run-config", ".tmp");

        // Write temporary config file
        BufferedWriter configWriter = new BufferedWriter(new FileWriter(tempConfig));
        configWriter.write("zzz=5");
        configWriter.close();

        // Create with a duplicate between the two sets
        boolean thrown = false;
        try {
            new NBProperties(tempConfig.getAbsolutePath(),
                    new String[]{"abc", "def"},
                    new String[]{"deF", "xyz"}
            );
        } catch (PropertyNotExistingException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Clean-up
        assertTrue(tempConfig.delete());

    }

    @Test
    public void testValueFetching() throws IOException {

        // Create temporary files
        File tempConfig = File.createTempFile("temp-run-config", ".tmp");

        // Write temporary config file
        BufferedWriter configWriter = new BufferedWriter(new FileWriter(tempConfig));
        configWriter.write("abc=33\nxyz=test\n643=33.13\nZZZ=9\nXyz=Test\nkey=4294967296669\nbb=false\ncc=true\nscenario_topology_file=xyz");
        configWriter.close();

        NBProperties properties = new NBProperties(tempConfig.getAbsolutePath(), TEST_ALLOWED_PROPERTIES);

        // File name
        assertEquals(tempConfig.getAbsolutePath(), properties.getFileName());

        // Test definition
        assertTrue(properties.isPropertyDefined("xyz"));
        assertFalse(properties.isPropertyDefined("zzz"));

        // Test defaults (no hit)
        assertEquals(properties.getIntegerPropertyWithDefault("zzz", 100), 100);
        assertEquals(properties.getLongPropertyWithDefault("zzz", 100L), 100L);
        assertEquals(properties.getDoublePropertyWithDefault("zzz", -445.3), -445.3, 1e-10);
        assertEquals(properties.getPropertyWithDefault("zzz", "aaa"), "aaa");
        assertEquals(properties.getBooleanPropertyWithDefault("zzz", true), true);

        // Test defaults (with hit)
        assertEquals(properties.getIntegerPropertyWithDefault("abc", 100), 33);
        assertEquals(properties.getLongPropertyWithDefault("key", 100L), 4294967296669L);
        assertEquals(properties.getDoublePropertyWithDefault("643", -445.3), 33.13, 1e-10);
        assertEquals(properties.getPropertyWithDefault("Xyz", "aaa"), "Test");
        assertEquals(properties.getBooleanPropertyWithDefault("bb", true), false);
        assertEquals(properties.getBooleanPropertyWithDefault("cc", false), true);

        // Test with fail
        assertEquals(properties.getIntegerPropertyOrFail("abc"), 33);
        assertEquals(properties.getPropertyOrFail("xyz"), "test");
        assertEquals(properties.getLongPropertyOrFail("key"), 4294967296669L);
        assertEquals(properties.getDoublePropertyOrFail("643"), 33.13, 1e-10);
        assertEquals(properties.getIntegerPropertyOrFail("ZZZ"), 9);
        assertEquals(properties.getPropertyOrFail("Xyz"), "Test");
        assertEquals(properties.getBooleanPropertyOrFail("bb"), false);
        assertEquals(properties.getBooleanPropertyOrFail("cc"), true);

        // Actual fail due to property missing in configuration (but is allowed)
        boolean thrown = false;
        try {
            properties.getPropertyOrFail("zzz");
        } catch (PropertyMissingException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Actual fail due to invalid boolean value
        thrown = false;
        try {
            properties.getBooleanPropertyOrFail("643");
        } catch (PropertyValueInvalidException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Actual fail due to invalid boolean value
        thrown = false;
        try {
            properties.getBooleanPropertyWithDefault("643", false);
        } catch (PropertyValueInvalidException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Property overriding
        properties.overrideProperty("bb", "15.0");
        assertEquals(properties.getDoublePropertyOrFail("bb"), 15.0, 1e-10);
        properties.overrideProperty("zzz", "false");
        assertEquals(properties.getBooleanPropertyOrFail("zzz"), false);

        // Configuration file name
        assertEquals(tempConfig.getAbsolutePath(), properties.getFileName());
        assertEquals("xyz", properties.getTopologyFileNameOrFail());

        // Delete temporary config file
        assertTrue(tempConfig.delete());

    }

    @Test
    public void testDirectedList() throws IOException {

        // Create temporary files
        File tempConfig = File.createTempFile("temp-run-config", ".tmp");

        // Write temporary config file
        BufferedWriter configWriter = new BufferedWriter(new FileWriter(tempConfig));
        configWriter.write("abc=3->10,9->11");
        configWriter.close();

        NBProperties properties = new NBProperties(tempConfig.getAbsolutePath(), TEST_ALLOWED_PROPERTIES);
        List<Integer> list = properties.getDirectedPairsListPropertyOrFail("abc");
        assertEquals(4, list.size());
        assertEquals(3, (int) list.get(0));
        assertEquals(10, (int) list.get(1));
        assertEquals(9, (int) list.get(2));
        assertEquals(11, (int) list.get(3));

        // Clean-up
        assertTrue(tempConfig.delete());

    }

    @Test
    public void testSimpleGraph() throws IOException {

        // Create temporary files
        File tempConfig = File.createTempFile("temp-run-config", ".tmp");
        File tempTopology = File.createTempFile("topology", ".tmp");

        // Write temporary config file
        BufferedWriter topologyWriter = new BufferedWriter(new FileWriter(tempTopology));
        topologyWriter.write("|V|=4\n");
        topologyWriter.write("|E|=5\n");
        topologyWriter.write("Servers=incl_range(0, 3)\n");
        topologyWriter.write("Switches=set()\n");
        topologyWriter.write("ToRs=incl_range(0, 3)\n");
        topologyWriter.write("0 1\n");
        topologyWriter.write("1 2\n");
        topologyWriter.write("2 3\n");
        topologyWriter.write("3 1\n");
        topologyWriter.write("3 2");
        topologyWriter.close();

        // Write temporary config file
        BufferedWriter configWriter = new BufferedWriter(new FileWriter(tempConfig));
        configWriter.write("scenario_topology_file=" + tempTopology.getAbsolutePath().replace("\\", "/"));
        configWriter.close();

        // Create properties
        NBProperties properties = new NBProperties(tempConfig.getAbsolutePath(), BaseAllowedProperties.PROPERTIES_RUN);

        // Get graph
        properties.getGraph();
        GraphDetails details = properties.getGraphDetails();
        assertEquals(details.getNumNodes(), 4);
        assertEquals(details.getNumEdges(), 5);

        // Clean-up
        assertTrue(tempConfig.delete());

    }

}
