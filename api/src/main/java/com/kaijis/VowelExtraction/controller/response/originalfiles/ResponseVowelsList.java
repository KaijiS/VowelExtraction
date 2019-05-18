package com.kaijis.VowelExtraction.controller.response.originalfiles;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ResponseVowelsList {

    private Long    id;

    private String  filename;

    private String  wavefile;

    private String  textfile;

    private String  labfile;

    private String  logfile;

    private String  uploadAt;

    ArrayList<ResponseVowelInfo> voweldata = new ArrayList<ResponseVowelInfo>();

}
