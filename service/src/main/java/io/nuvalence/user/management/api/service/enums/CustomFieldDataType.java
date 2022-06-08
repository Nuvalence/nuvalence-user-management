package io.nuvalence.user.management.api.service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the possible data types for custom fields.
 */
public enum CustomFieldDataType {
    STRING("string"),
    INT("int"),
    JSON("json"),
    DATETIME("datetime");

    @JsonValue
    private final String type;

    CustomFieldDataType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    /**
     * Converts between strings and CustomFieldDataType enum.
     *
     * @param text the text representation of the enum.
     * @return an enum value.
     */
    public static CustomFieldDataType fromText(String text) {
        for (CustomFieldDataType dataType : CustomFieldDataType.values()) {
            if (dataType.toString().equalsIgnoreCase(text)) {
                return dataType;
            }
        }

        return STRING;
    }
}
