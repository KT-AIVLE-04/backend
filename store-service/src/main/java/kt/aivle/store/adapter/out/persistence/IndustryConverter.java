package kt.aivle.store.adapter.out.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.store.domain.model.Industry;

import java.util.Arrays;

import static kt.aivle.store.exception.StoreErrorCode.NOT_FOUND_INDUSTRY;

@Converter(autoApply = true)
public class IndustryConverter implements AttributeConverter<Industry, String> {

    @Override
    public String convertToDatabaseColumn(Industry attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Industry convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return Arrays.stream(Industry.values())
                .filter(e -> e.getValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new BusinessException(NOT_FOUND_INDUSTRY, dbData));
    }
}