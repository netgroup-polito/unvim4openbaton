package org.polito.model.yang.nat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NatYang {
    @JsonProperty("config-nat:interfaces")
    private ConfigNatInterfaces configNatInterfaces = new ConfigNatInterfaces();
    @JsonProperty("config-nat:staticBindings")
    private ConfigNatStaticBindings configNatStaticBindings = new ConfigNatStaticBindings();

    @JsonProperty("config-nat:interfaces")
    public ConfigNatInterfaces getConfigNatInterfaces() {
        return configNatInterfaces;
    }

    @JsonProperty("config-nat:interfaces")
    public void setConfigNatInterfaces(ConfigNatInterfaces configNatInterfaces) {
        this.configNatInterfaces = configNatInterfaces;
    }

	public ConfigNatStaticBindings getConfigNatStaticBindings() {
		return configNatStaticBindings;
	}

	public void setConfigNatStaticBindings(ConfigNatStaticBindings configNatStaticBindings) {
		this.configNatStaticBindings = configNatStaticBindings;
	}

}
