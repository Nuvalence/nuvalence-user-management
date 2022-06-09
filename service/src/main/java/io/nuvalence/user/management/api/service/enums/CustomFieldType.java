package io.nuvalence.user.management.api.service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the possible types for custom fields.
 */
public enum CustomFieldType {
    DROP_DOWN_LIST("drop_down_list"),
    TEXT_FIELD("text_field");

    @JsonValue
    private final String type;

    CustomFieldType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    /**
     * Converts between strings and CustomFieldType enum.
     *
     * @param text the text representation of the enum.
     * @return an enum value.
     */
    public static CustomFieldType fromText(String text) {
        for (CustomFieldType type : CustomFieldType.values()) {
            if (type.toString().equalsIgnoreCase(text)) {
                return type;
            }
        }

        return TEXT_FIELD;
    }
}
