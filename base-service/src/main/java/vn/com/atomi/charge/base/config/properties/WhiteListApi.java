package vn.com.atomi.charge.base.config.properties;

import lombok.Data;

import java.util.List;

@Data
public class WhiteListApi {

    private List<String> whiteList;
}