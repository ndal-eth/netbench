package ch.ethz.systems.netbench.core.config.exceptions;

import ch.ethz.systems.netbench.core.config.NBProperties;
import org.apache.commons.lang3.StringUtils;

public class PropertyConflictException extends RuntimeException {

    public PropertyConflictException(NBProperties properties, String... keys) {
        super("[" + properties.getFileName() + "]: properties {" +
                StringUtils.join(keys, ", ") + "} conflict in the configuration file.");
    }

}
