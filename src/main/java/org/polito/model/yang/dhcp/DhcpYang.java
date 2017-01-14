
package org.polito.model.yang.dhcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "config-dhcp-server:server",
    "config-dhcp-server:interfaces"
})
public class DhcpYang {

    @JsonProperty("config-dhcp-server:server")
    private ConfigDhcpServerServer configDhcpServerServer = new ConfigDhcpServerServer();
    @JsonProperty("config-dhcp-server:interfaces")
    private ConfigDhcpServerInterfaces configDhcpServerInterfaces = new ConfigDhcpServerInterfaces();

    @JsonProperty("config-dhcp-server:server")
    public ConfigDhcpServerServer getConfigDhcpServerServer() {
        return configDhcpServerServer;
    }

    @JsonProperty("config-dhcp-server:server")
    public void setConfigDhcpServerServer(ConfigDhcpServerServer configDhcpServerServer) {
        this.configDhcpServerServer = configDhcpServerServer;
    }

    @JsonProperty("config-dhcp-server:interfaces")
    public ConfigDhcpServerInterfaces getConfigDhcpServerInterfaces() {
        return configDhcpServerInterfaces;
    }

    @JsonProperty("config-dhcp-server:interfaces")
    public void setConfigDhcpServerInterfaces(ConfigDhcpServerInterfaces configDhcpServerInterfaces) {
        this.configDhcpServerInterfaces = configDhcpServerInterfaces;
    }

}
