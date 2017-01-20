package org.polito.model.yang.nat;

import java.util.ArrayList;
import java.util.List;

import org.polito.model.yang.IfEntry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigNatInterfaces {

    @JsonProperty("ifEntry")
    private List<IfEntry> ifEntry = new ArrayList<IfEntry>();

    @JsonProperty("ifEntry")
    public List<IfEntry> getIfEntry() {
        return ifEntry;
    }

    @JsonProperty("ifEntry")
    public void setIfEntry(List<IfEntry> ifEntry) {
        this.ifEntry = ifEntry;
    }

}
