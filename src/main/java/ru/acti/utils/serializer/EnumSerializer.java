package ru.acti.utils.serializer;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import ru.acti.entity.ErrorCodeEnum;

import java.io.IOException;

public class EnumSerializer extends JsonSerializer<ErrorCodeEnum> {

    @Override
    public void serialize(ErrorCodeEnum value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeNumber(value.getCode());
    }
}
