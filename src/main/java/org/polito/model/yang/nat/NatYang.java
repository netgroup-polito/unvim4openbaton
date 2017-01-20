package org.polito.model.yang.nat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NatYang {

    @JsonProperty("config-nat:interfaces")
    private ConfigNatInterfaces configNatInterfaces = new ConfigNatInterfaces();

    @JsonProperty("config-nat:interfaces")
    public ConfigNatInterfaces getConfigNatInterfaces() {
        return configNatInterfaces;
    }

    @JsonProperty("config-nat:interfaces")
    public void setConfigNatInterfaces(ConfigNatInterfaces configNatInterfaces) {
        this.configNatInterfaces = configNatInterfaces;
    }

}
