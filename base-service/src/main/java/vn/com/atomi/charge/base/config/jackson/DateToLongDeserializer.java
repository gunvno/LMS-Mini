package vn.com.atomi.charge.base.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import vn.com.atomi.charge.base.util.DateUtil;
import vn.com.atomi.charge.base.util.Util;

import java.io.IOException;

public class DateToLongDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String dateString = jsonParser.getText();
        return Util.getUnixTime(dateString, DateUtil.YMD_HMS_ISO_FORMAT);
    }
}