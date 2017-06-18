package ch.ethz.systems.netbench.xpt.utility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TwoExclusionRangeTest {

    @Test
    public void testNoExclusions() {

        TwoExclusionRange range = new TwoExclusionRange(0, 99, 120);
        for (int i = 0; i < 200; i++) {
            assertEquals(range.draw(i, 100), i % 100);
        }

    }

    @Test
    public void testExclusionFromSrc() {

        TwoExclusionRange range = new TwoExclusionRange(0, 99, 30);
        for (int i = 0; i < 100; i++) {
            if (i < 30) {
                assertEquals(range.draw(i, 100), i % 100);
            } else {
                assertEquals(range.draw(i, 100), (i + 1) % 100);
            }
        }

    }

    @Test
    public void testExclusionFromDst() {

        TwoExclusionRange range = new TwoExclusionRange(0, 99, 100);
        for (int i = 0; i < 100; i++) {
            if (i < 55) {
                assertEquals(i % 100, range.draw(i, 55));
            } else {
                assertEquals((i + 1) % 100, range.draw(i, 55));
            }
        }

    }

    @Test
    public void testExclusionFromSrcDst() {

        TwoExclusionRange range = new TwoExclusionRange(0, 99, 33);
        for (int i = 0; i < 100; i++) {
            if (i < 33) {
                assertEquals(i % 100, range.draw(i, 55));
            } else if (i < 54) {
                assertEquals((i + 1) % 100, range.draw(i, 55));
            } else {
                assertEquals((i + 2) % 100, range.draw(i, 55));
            }
        }

    }



}
