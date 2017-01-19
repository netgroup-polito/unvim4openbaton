package org.polito.model.yang.nat;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IfEntry {

    @JsonProperty("name")
    private String name;
    @JsonProperty("configurationType")
    private String configurationType;
    @JsonProperty("type")
    private String type;
    @JsonProperty("address")
    private String address;
    @JsonProperty("default_gw")
    private String defaultGw;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
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

    @JsonProperty("address")
    public String getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonProperty("default_gw")
    public String getDefaultGw() {
        return defaultGw;
    }

    @JsonProperty("default_gw")
    public void setDefaultGw(String defaultGw) {
        this.defaultGw = defaultGw;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
