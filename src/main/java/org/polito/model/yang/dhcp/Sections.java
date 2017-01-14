
package org.polito.model.yang.dhcp;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "section"
})
public class Sections {

    @JsonProperty("section")
    private List<Section> section = new ArrayList<Section>();

    @JsonProperty("section")
    public List<Section> getSection() {
        return section;
    }

    @JsonProperty("section")
    public void setSection(List<Section> section) {
        this.section = section;
    }

}
