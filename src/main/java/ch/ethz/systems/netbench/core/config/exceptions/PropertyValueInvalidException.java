package ch.ethz.systems.netbench.core.config.exceptions;

import ch.ethz.systems.netbench.core.config.NBProperties;

public class PropertyValueInvalidException extends RuntimeException {

    public PropertyValueInvalidException(NBProperties properties, String key) {
        super("[" + properties.getFileName() + "]: essential property \"" + key + "\" has invalid value \"" + properties.getPropertyOrFail(key) + "\".");
    }

}