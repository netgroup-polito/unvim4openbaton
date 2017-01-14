
package org.polito.model.yang.dhcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "gatewayIp",
    "sections",
    "defaultLeaseTime",
    "maxLeaseTime",
    "domainNameServer",
    "domainName"
})
public class GlobalIpPool {

    @JsonProperty("gatewayIp")
    private GatewayIp gatewayIp = new GatewayIp();
    @JsonProperty("sections")
    private Sections sections = new Sections();
    @JsonProperty("defaultLeaseTime")
    private String defaultLeaseTime;
    @JsonProperty("maxLeaseTime")
    private String maxLeaseTime;
    @JsonProperty("domainNameServer")
    private String domainNameServer;
    @JsonProperty("domainName")
    private String domainName;

    @JsonProperty("gatewayIp")
    public GatewayIp getGatewayIp() {
        return gatewayIp;
    }

    @JsonProperty("gatewayIp")
    public void setGatewayIp(GatewayIp gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    @JsonProperty("sections")
    public Sections getSections() {
        return sections;
    }

    @JsonProperty("sections")
    public void setSections(Sections sections) {
        this.sections = sections;
    }

    @JsonProperty("defaultLeaseTime")
    public String getDefaultLeaseTime() {
        return defaultLeaseTime;
    }

    @JsonProperty("defaultLeaseTime")
    public void setDefaultLeaseTime(String defaultLeaseTime) {
        this.defaultLeaseTime = defaultLeaseTime;
    }

    @JsonProperty("maxLeaseTime")
    public String getMaxLeaseTime() {
        return maxLeaseTime;
    }

    @JsonProperty("maxLeaseTime")
    public void setMaxLeaseTime(String maxLeaseTime) {
        this.maxLeaseTime = maxLeaseTime;
    }

    @JsonProperty("domainNameServer")
    public String getDomainNameServer() {
        return domainNameServer;
    }

    @JsonProperty("domainNameServer")
    public void setDomainNameServer(String domainNameServer) {
        this.domainNameServer = domainNameServer;
    }

    @JsonProperty("domainName")
    public String getDomainName() {
        return domainName;
    }

    @JsonProperty("domainName")
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

}
