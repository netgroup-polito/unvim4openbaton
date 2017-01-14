
package org.polito.model.yang.dhcp;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ifEntry"
})
public class ConfigDhcpServerInterfaces {

    @JsonProperty("ifEntry")
    private List<IfEntry> ifEntry = new ArrayList<>();

    @JsonProperty("ifEntry")
    public List<IfEntry> getIfEntry() {
        return ifEntry;
    }

    @JsonProperty("ifEntry")
    public void setIfEntry(List<IfEntry> ifEntry) {
        this.ifEntry = ifEntry;
    }

}
