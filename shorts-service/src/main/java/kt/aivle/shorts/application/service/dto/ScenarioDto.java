package kt.aivle.shorts.application.service.dto;

import kt.aivle.shorts.adapter.in.event.StoreInfoResponseEvent;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import lombok.Builder;
import org.springframework.http.codec.multipart.FilePart;

import java.util.List;

@Builder
public record ScenarioDto(String store_name,
                          String store_address,
                          String store_contact_number,
                          String business_type,
                          List<String> brand_concept,
                          List<FilePart> image_list,
                          String platform,
                          String ad_type,
                          String target_audience,
                          String scenario_prompt
) {
    public static ScenarioDto from(
            CreateScenarioCommand command,
            StoreInfoResponseEvent storeInfo,
            List<FilePart> image_list
    ) {
        return ScenarioDto.builder()
                .store_name(storeInfo.name())
                .store_address(storeInfo.address())
                .store_contact_number(storeInfo.phoneNumber())
                .business_type(storeInfo.industry())
                .brand_concept(command.brandConcepts())
                .image_list(image_list)
                .platform(command.platform())
                .ad_type(command.promotionType())
                .target_audience(command.target())
                .scenario_prompt(command.prompt())
                .build();
    }
}
