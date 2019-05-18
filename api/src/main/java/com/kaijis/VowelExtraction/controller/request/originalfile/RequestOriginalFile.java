package com.kaijis.VowelExtraction.controller.request.originalfile;


import lombok.Data;

@Data
public class RequestOriginalFile {

    private String wavedata;

    private String waveFilename;

    private String textdata;

    private String textFilename;

    private String vowel;

}
