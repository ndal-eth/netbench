package ch.ethz.systems.netbench.core.config.exceptions;

import ch.ethz.systems.netbench.core.config.NBProperties;

public class PropertyNotExistingException extends RuntimeException {

    public PropertyNotExistingException(NBProperties properties, String key) {
        super("[" + properties.getFileName() + "]: property \"" + key + "\" is not a valid property key. " +
                "Three possibilities: (a) Did you make a typo in the code? " +
                "(b) Did you make a typo in the configuration file? " +
                "(c) Did you introduce your own property and forgot to add it to the static list in the NBProperties class?"
        );
    }

}
