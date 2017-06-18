package ch.ethz.systems.netbench.core.log;

import java.io.IOException;

/**
 * A logging action has failed, presumably due to failure of an I/O action.
 */
public class LogFailureException extends RuntimeException {

    public LogFailureException(IOException cause) {
        super(cause.getMessage());
    }

}
