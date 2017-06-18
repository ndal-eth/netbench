package ch.ethz.systems.netbench.core.config.exceptions;

import ch.ethz.systems.netbench.core.config.NBProperties;

import java.io.IOException;

public class ConfigurationReadFailException extends RuntimeException {

    public ConfigurationReadFailException(NBProperties properties, IOException cause) {
        super("[" + properties.getFileName() + "]: " + cause.getMessage());
    }

}
