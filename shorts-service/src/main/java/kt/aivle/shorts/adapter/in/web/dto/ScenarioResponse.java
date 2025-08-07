package kt.aivle.shorts.adapter.in.web.dto;

import java.util.List;

public record ScenarioResponse(String session_id, List<Scenario> scenarios) {
}
