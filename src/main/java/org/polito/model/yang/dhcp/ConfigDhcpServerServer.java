
package org.polito.model.yang.dhcp;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "globalIpPool",
    "clients"
})
public class ConfigDhcpServerServer {

    @JsonProperty("globalIpPool")
    private GlobalIpPool globalIpPool = new GlobalIpPool();
    @JsonProperty("clients")
    private List<Object> clients = new ArrayList<>();

    @JsonProperty("globalIpPool")
    public GlobalIpPool getGlobalIpPool() {
        return globalIpPool;
    }

    @JsonProperty("globalIpPool")
    public void setGlobalIpPool(GlobalIpPool globalIpPool) {
        this.globalIpPool = globalIpPool;
    }

    @JsonProperty("clients")
    public List<Object> getClients() {
        return clients;
    }

    @JsonProperty("clients")
    public void setClients(List<Object> clients) {
        this.clients = clients;
    }

}
