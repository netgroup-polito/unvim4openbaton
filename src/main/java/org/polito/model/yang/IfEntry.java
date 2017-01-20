
package org.polito.model.yang;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "address",
    "configurationType",
    "type",
    "default_gw"
})
public class IfEntry {

    @JsonProperty("name")
    private String name;
    @JsonProperty("address")
    private String address;
    @JsonProperty("configurationType")
    private String configurationType;
    @JsonProperty("type")
    private String type;
    @JsonProperty("default_gw")
    private String defaultGw;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("address")
    public String getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonProperty("configurationType")
    public String getConfigurationType() {
        return configurationType;
    }

    @JsonProperty("configurationType")
    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("default_gw")
    public String getDefaultGw() {
        return defaultGw;
    }

    @JsonProperty("default_gw")
    public void setDefaultGw(String defaultGw) {
        this.defaultGw = defaultGw;
    }

}
