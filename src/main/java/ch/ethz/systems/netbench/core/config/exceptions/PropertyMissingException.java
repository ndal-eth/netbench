package ch.ethz.systems.netbench.core.config.exceptions;

import ch.ethz.systems.netbench.core.config.NBProperties;

public class PropertyMissingException extends RuntimeException {

    public PropertyMissingException(NBProperties config, String key) {
        super("[" + config.getFileName() + "]: essential property \"" + key + "\" was not found in the configuration file.");
    }

}
