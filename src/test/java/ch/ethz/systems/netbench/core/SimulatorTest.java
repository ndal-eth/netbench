package ch.ethz.systems.netbench.core;

import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.core.utility.UnitConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SimulatorTest {

    @Test
    public void testIndependentRng() {

        Simulator.setup(333);

        Random rng = Simulator.selectIndependentRandom("flow_size");
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            numbers.add(rng.nextInt());
        }

        Simulator.reset();
        Simulator.setup(333);

        Random rng2 = Simulator.selectIndependentRandom("flow_size");
        for (int i = 0; i < 1000; i++) {
            assertEquals((int) numbers.get(i), (int) rng2.nextInt());
        }


        Simulator.reset();
    }

    @Test
    public void testRunSimple() {
        Simulator.setup(0);
        Simulator.runNs(UnitConverter.convertSecondsToNanoseconds(2));
        assertEquals(2000000000L, Simulator.getCurrentTime());
        assertEquals(0, Simulator.getEventSize());
        Simulator.reset();
        Simulator.setup(0);
        assertEquals(0, Simulator.getCurrentTime());
        Simulator.reset();
    }

    private class TestEvent extends Event {

        /**
         * Create event which will happen the given amount of nanoseconds later.
         *
         * @param timeFromNowNs Time it will take before happening from now in nanoseconds
         */
        TestEvent(long timeFromNowNs) {
            super(timeFromNowNs);
        }

        @Override
        public void trigger() {
            assert(this.getTime() == Simulator.getCurrentTime());
        }

    }

    @Test
    public void testRunSimpleWithEvents() {

        // Event that do not happen
        Simulator.setup(0);
        Simulator.registerEvent(new TestEvent(0));
        Simulator.registerEvent(new TestEvent(9001));
        Simulator.registerEvent(new TestEvent(13000));
        Simulator.registerEvent(new TestEvent(999999999));
        Simulator.registerEvent(new TestEvent(1000000000));
        Simulator.registerEvent(new TestEvent(1000000001));
        Simulator.registerEvent(new TestEvent(2000000000));
        assertEquals(Simulator.getEventSize(), 7);
        Simulator.runNs(UnitConverter.convertSecondsToNanoseconds(1));
        assertEquals(Simulator.getEventSize(), 2);
        Simulator.reset();

        // All events should have done
        Simulator.setup(0);
        assertEquals(Simulator.getEventSize(), 0);
        Simulator.registerEvent(new TestEvent(0));
        Simulator.registerEvent(new TestEvent(9001));
        Simulator.registerEvent(new TestEvent(13000));
        Simulator.registerEvent(new TestEvent(999999999));
        Simulator.registerEvent(new TestEvent(1000000001));
        assertEquals(Simulator.getEventSize(), 5);
        Simulator.runNs(UnitConverter.convertSecondsToNanoseconds(2));
        assertEquals(Simulator.getEventSize(), 0);
        Simulator.reset();

    }

    @Test
    public void testConfiguration() throws IOException {

        // Create two temporary files
        File tempConfig = File.createTempFile("temp-run-config", ".tmp");

        // Write temporary config file
        BufferedWriter configWriter = new BufferedWriter(new FileWriter(tempConfig));
        configWriter.write("run_time_s=33525\ntransport_layer=573748848test");
        configWriter.close();

        Simulator.setup(0, new NBProperties(tempConfig.getAbsolutePath(), BaseAllowedProperties.LOG, BaseAllowedProperties.PROPERTIES_RUN));
        assertEquals(Simulator.getConfiguration().getPropertyOrFail("run_time_s"), "33525");
        assertEquals(Simulator.getConfiguration().getPropertyOrFail("transport_layer"), "573748848test");
        Simulator.reset();

        // Delete temporary files
        assertTrue(tempConfig.delete());



    }

    @Test
    public void testDoubleSetupIllegal() throws IOException {

        Simulator.setup(0);
        boolean thrown = false;
        try {
            Simulator.setup(0);
        } catch (RuntimeException e) {
            thrown = true;
        }
        assertTrue(thrown);
        Simulator.reset();

    }

}
