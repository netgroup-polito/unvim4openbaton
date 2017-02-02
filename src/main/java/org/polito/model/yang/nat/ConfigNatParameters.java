package org.polito.model.yang.nat;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigNatParameters {

    @JsonProperty("floating-ip")
    private List<FloatingIp> floatingIp = new ArrayList<FloatingIp>();

    public List<FloatingIp> getFloatingIp() {
        return floatingIp;
    }

    public void setFloatingIp(List<FloatingIp> floatingIp) {
        this.floatingIp = floatingIp;
    }

}
