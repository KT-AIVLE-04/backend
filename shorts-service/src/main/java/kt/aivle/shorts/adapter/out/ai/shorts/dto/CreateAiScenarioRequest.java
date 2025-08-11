package kt.aivle.shorts.adapter.out.ai.shorts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateAiScenarioRequest(
        @JsonProperty("store_name") String storeName,
        @JsonProperty("business_type") String businessType,
        @JsonProperty("brand_concept") List<String> brandConcept,
        @JsonProperty("image_list") List<String> imageList,
        @JsonProperty("platform") String platform,
        @JsonProperty("ad_type") String adType,
        @JsonProperty("target_audience") String targetAudience,
        @JsonProperty("scenario_prompt") String scenarioPrompt
) {
}
