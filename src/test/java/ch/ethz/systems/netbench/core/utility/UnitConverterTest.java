package ch.ethz.systems.netbench.core.utility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UnitConverterTest {

    @Test
    public void testConversion() {
        assertEquals(UnitConverter.convertSecondsToNanoseconds(-1), -1000000000L);
        assertEquals(UnitConverter.convertSecondsToNanoseconds(0), 0L);
        assertEquals(UnitConverter.convertSecondsToNanoseconds(7), 7000000000L);
        assertEquals(UnitConverter.convertSecondsToNanoseconds(-1.34), -1340000000L);
        assertEquals(UnitConverter.convertSecondsToNanoseconds(0.0), 0L);
        assertEquals(UnitConverter.convertSecondsToNanoseconds(6.99), 6990000000L);
    }

}
