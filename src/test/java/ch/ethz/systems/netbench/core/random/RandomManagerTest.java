package ch.ethz.systems.netbench.core.random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RandomManagerTest {

    @Test
    public void testSameSeed() {

        RandomManager manager1 = new RandomManager(300);
        RandomManager manager2 = new RandomManager(300);

        Random r1 = manager1.getRandom("test");
        Random r2 = manager2.getRandom("test");

        for (int i = 0; i < 1000; i++) {
            assertEquals(r1.nextLong(), r2.nextLong());
        }

    }

    @Test
    public void testRequestAgain() {

        RandomManager manager = new RandomManager(300);
        manager.getRandom("test");
        boolean thrown = false;
        try {
            manager.getRandom("test");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void weakTestCollision() {
        RandomManager manager = new RandomManager(11);
        for (int i = 0; i < 1000; i++) {
            manager.getRandom(String.valueOf(i));
        }
    }

}
