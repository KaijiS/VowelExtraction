package com.kaijis.VowelExtraction.library;

import lombok.Data;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Wave {

    private byte[]  bin;                     // バイナリデータ
    private String  riff;                    // RIFF識別子 “RIFF”(0x52494646)で固定。
    private String  format;                  // フォーマット WAVファイルの場合は“WAVE”(0x57415645)で固定。AVIファイルの場合は“AVI”が入る
    private String  fmt;                     // fmt識別子 “fmt “(0x666D7420)で固定
    private Integer audioFormat;             // 音声フォーマット 非圧縮のリニアPCMフォーマットは1(0x0100) 他、例えばA-lawは6、μ-lawは7
    private Integer ch;                      // チャンネル数 モノラルは1(0x0100)、ステレオは2(0x0200)
    private Integer fs;                      // サンプリング周波数(Hz) 8kHzの場合は(0x401F0000)、44.1kHzの場合なら(0x44AC0000)
    private Integer quantization_bit;        // 量子化ビット数(ビット／サンプル) 1サンプルに必要なビット数 8ビットの場合は8(0x0800)、16ビットの場合は16(0x1000)など
    private String  subchunk_identifier;     // サブチャンク識別子 “data” (0x64617461)で固定
    private Integer subchunk_size_of_wave;   // サブチャンクザイズ 波形データのバイト数(総ファイルサイズ – 126)

    public Wave(byte[]  wavedata){
        this.bin    = wavedata;
        this.riff   = new String(Arrays.copyOfRange(wavedata,0, 4));
        this.format = new String(Arrays.copyOfRange(wavedata,8, 12));
        this.fmt    = new String(Arrays.copyOfRange(wavedata,12,16));
        this.audioFormat     = Integer.parseInt(DatatypeConverter.printHexBinary(this.toLittleEndian(Arrays.copyOfRange(wavedata,20,22))).toLowerCase(),16);
        this.ch     = Integer.parseInt(DatatypeConverter.printHexBinary(this.toLittleEndian(Arrays.copyOfRange(wavedata,22,24))).toLowerCase(),16);
        this.fs     = Integer.parseInt(DatatypeConverter.printHexBinary(this.toLittleEndian(Arrays.copyOfRange(wavedata,24,28))).toLowerCase(),16);
        this.quantization_bit       = Integer.parseInt(DatatypeConverter.printHexBinary(this.toLittleEndian(Arrays.copyOfRange(wavedata,34,36))).toLowerCase(),16);
        this.subchunk_identifier    = new String(Arrays.copyOfRange(wavedata,36,40));
        this.subchunk_size_of_wave  = Integer.parseInt(DatatypeConverter.printHexBinary(this.toLittleEndian(Arrays.copyOfRange(wavedata,40,44))).toLowerCase(),16);

    }


    public List<String> check(){
        List<String> errorMessages = new ArrayList<String>();;

        if ( ! (this.riff.equals("RIFF") && this.fmt.equals("fmt ") && this.subchunk_identifier.equals("data"))) {
            errorMessages.add("ファイル形式が違います");
        }

        if ( ! this.format.equals("WAVE") ){
            errorMessages.add("wavファイルにしてください");
        }

//         if (this.audioFormat != 1) {
//             errorMessages.add("非圧縮のリニアPCMフォーマットにしてください");
//         }

        // チャンネル数を取得
        // モノラルチャンネルだけが対象
        if (this.ch != 1) {
            errorMessages.add("モノラルにしてください");
        }

        // サンプリング周波数を取得
        // 16000[Hz]だけが対象
        if (this.fs != 16000) {
            errorMessages.add("サンプリング周波数は16000[Hz]にしてください");
        }

        // 量子化bit数を取得
        // 16bitだけが対象
        if (this.quantization_bit != 16) {
            errorMessages.add("量子化ビット数は16[bit]にしてください");
        }

        return errorMessages;
    }

    private byte[] toLittleEndian(byte[] byteArrayOfBigEndian){
        byte[] byteArrayOfLittleEndian = new byte[byteArrayOfBigEndian.length];

        for (int i=0; i<byteArrayOfBigEndian.length; i++){
            byteArrayOfLittleEndian[i] = byteArrayOfBigEndian[byteArrayOfBigEndian.length-i-1];
        }

        return byteArrayOfLittleEndian;

    }


}
