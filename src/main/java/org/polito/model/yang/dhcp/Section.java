
package org.polito.model.yang.dhcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sectionStartIp",
    "sectionEndIp"
})
public class Section {

    @JsonProperty("sectionStartIp")
    private String sectionStartIp;
    @JsonProperty("sectionEndIp")
    private String sectionEndIp;

    @JsonProperty("sectionStartIp")
    public String getSectionStartIp() {
        return sectionStartIp;
    }

    @JsonProperty("sectionStartIp")
    public void setSectionStartIp(String sectionStartIp) {
        this.sectionStartIp = sectionStartIp;
    }

    @JsonProperty("sectionEndIp")
    public String getSectionEndIp() {
        return sectionEndIp;
    }

    @JsonProperty("sectionEndIp")
    public void setSectionEndIp(String sectionEndIp) {
        this.sectionEndIp = sectionEndIp;
    }

}
