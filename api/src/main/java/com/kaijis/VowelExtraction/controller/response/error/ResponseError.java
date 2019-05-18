package com.kaijis.VowelExtraction.controller.response.error;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseError {

    private List<String> messages = new ArrayList<>();

}
