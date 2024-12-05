package net.larsan.ai.api;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.gson.JsonObject;
import com.google.protobuf.Value;

public record MetadataField(
        String name,
        @Schema(implementation = Object.class) ValueNode value) {

    public Value toProtobufValue() {
        if (value.isTextual()) {
            return Value.newBuilder().setStringValue(value.asText()).build();
        }
        if (value.isBoolean()) {
            return Value.newBuilder().setBoolValue(value.asBoolean()).build();
        }
        if (value.isNumber()) {
            return Value.newBuilder().setNumberValue(value.asDouble()).build();
        }
        throw new IllegalStateException("Illegal metadata type - " + value.getNodeType() + " - expected string, boolean or number");
    }

    @Schema(hidden = true)
    public void setAsGsonProperty(JsonObject parent) {
        if (value.isTextual()) {
            parent.addProperty(name, value.asText());
        } else if (value.isBoolean()) {
            parent.addProperty(name, value.asBoolean());
        } else if (value.isNumber()) {
            parent.addProperty(name, value.asDouble());
        } else {
            throw new IllegalStateException("Illegal metadata type - " + value.getNodeType() + " - expected string, boolean or number");
        }
    }
}
