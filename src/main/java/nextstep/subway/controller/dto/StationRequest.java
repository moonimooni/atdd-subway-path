package nextstep.subway.controller.dto;

import javax.validation.constraints.NotBlank;

public class StationRequest {
    @NotBlank
    private String name;

    public String getName() {
        return name;
    }
}
