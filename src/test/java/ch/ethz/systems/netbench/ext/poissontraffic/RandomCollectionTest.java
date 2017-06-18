package ch.ethz.systems.netbench.ext.poissontraffic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RandomCollectionTest {

    @Mock
    private Random random;

    @Test
    public void testAddInvalidNegativeWeight() {
        RandomCollection<Integer> rc = new RandomCollection<>(random);

        boolean thrown = false;
        try {
            rc.add(-0.0001, 894);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testAddAndNext() {

        RandomCollection<Integer> rc = new RandomCollection<>(random);
        rc.add(0.1, 894);
        rc.add(0.2, 34);
        rc.add(0.3, 11);
        rc.add(0.1, -378);
        rc.add(0.2, 88);
        rc.add(0.1, 95);

        when(random.nextDouble()).thenReturn(0.0);
        assertEquals(894, (int) rc.next());

        when(random.nextDouble()).thenReturn(0.09999999);
        assertEquals(894, (int) rc.next());

        when(random.nextDouble()).thenReturn(0.1);
        assertEquals(894, (int) rc.next());

        when(random.nextDouble()).thenReturn(0.15);
        assertEquals(34, (int) rc.next());

        when(random.nextDouble()).thenReturn(0.69999);
        assertEquals(-378, (int) rc.next());

        when(random.nextDouble()).thenReturn(0.7);
        assertEquals(-378, (int) rc.next());

        when(random.nextDouble()).thenReturn(0.9999);
        assertEquals(95, (int) rc.next());

        when(random.nextDouble()).thenReturn(1.0);
        assertEquals(95, (int) rc.next());

        boolean thrown = false;
        try {
            when(random.nextDouble()).thenReturn(1.00001);
            rc.next();
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

}
