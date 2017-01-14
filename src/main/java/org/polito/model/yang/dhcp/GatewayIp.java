
package org.polito.model.yang.dhcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "gatewayIp",
    "gatewayMask"
})
public class GatewayIp {

    @JsonProperty("gatewayIp")
    private String gatewayIp;
    @JsonProperty("gatewayMask")
    private String gatewayMask;

    @JsonProperty("gatewayIp")
    public String getGatewayIp() {
        return gatewayIp;
    }

    @JsonProperty("gatewayIp")
    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    @JsonProperty("gatewayMask")
    public String getGatewayMask() {
        return gatewayMask;
    }

    @JsonProperty("gatewayMask")
    public void setGatewayMask(String gatewayMask) {
        this.gatewayMask = gatewayMask;
    }

}
