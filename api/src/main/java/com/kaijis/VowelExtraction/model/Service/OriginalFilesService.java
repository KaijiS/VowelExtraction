package com.kaijis.VowelExtraction.model.Service;

import com.kaijis.VowelExtraction.model.Repository.OriginalFilesRepository;
import com.kaijis.VowelExtraction.model.entity.OriginalFiles;
import com.kaijis.VowelExtraction.model.mybatis.entity.OriginalFilesEntity;
import com.kaijis.VowelExtraction.model.mybatis.mapper.OriginalFilesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

@Service
public class OriginalFilesService {

    @Autowired
    private OriginalFilesRepository originalFilesRepository;

    @Autowired
    private OriginalFilesMapper originalFilesMapper;

    public Optional<OriginalFiles> findById(Long id) {
        return originalFilesRepository.findById(id);
    }

    public List<OriginalFiles> findByUserId(Long userId){
        return originalFilesRepository.findByUserId(userId);
    }

    public List<OriginalFiles> findByUserIdOrderByUploadAtDesc(Long userId) {
        return originalFilesRepository.findByUserIdOrderByUploadAtDesc(userId);
    }

    public Integer countByUserId(Long userId) {
        return originalFilesRepository.countByUserId(userId);
    }

    public List<OriginalFilesEntity> findByUserIdOrderByUploadAtDescLimitOffset(Long userId, Integer offset, Integer limit){
        return originalFilesMapper.findByUserIdOrderByUploadAtDescLimitOffset(userId, offset, limit);
    }

    public OriginalFiles save(OriginalFiles originalFiles) {
        return originalFilesRepository.save(originalFiles);
    }

    public void delete(OriginalFiles originalFiles) {
        originalFilesRepository.delete(originalFiles);
    }


    /**
     *  音声ファイルを取得しbase64へ変換
     */
    public String getWaveFileBase64(String filepath) throws IOException {
        File wavefile = new File(filepath);
        byte[] bytes = Files.readAllBytes(wavefile.toPath());
        return Base64.getEncoder().encodeToString(bytes); // base64へ変換
    }

    /**
     *  テキスト読み込み
     *
     * @param filepath, insertNewLineCode
     * @return json
     */
    public String readFile(String filepath, boolean insertNewLineCode) throws IOException {

        // BufferedReaderクラスのreadLineメソッドを使って1行ずつ読み込み表示する
        File file = new File(filepath);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String textdata = "";
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            textdata += line;
            if (insertNewLineCode) {
                textdata += "\n";
            }
        }
        bufferedReader.close();

        return textdata;
    }



    /**
     *  拡張子の抽出
     *
     * @param fileName
     * @return String
     */
    public String getExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        // 最後の『 . 』の位置を取得します。
        int lastDotPosition = fileName.lastIndexOf(".");

        // 『 . 』が存在する場合は、『 . 』以降を返します。
        if (lastDotPosition != -1) {
            return fileName.substring(lastDotPosition + 1);
        }
        return null;
    }

    /**
     *  拡張子のチェック
     *
     * @param waveFilename, textFilename
     * @return List<String>
     */
    public List<String> checkExtension(String waveFilename, String textFilename){

        List<String> errorMessages = new ArrayList<String>();
        String waveExtensions = this.getExtension(waveFilename);
        String textExtensions = this.getExtension(textFilename);


        if( !(waveExtensions.equals("wav") || waveExtensions.equals("WAV"))){
            errorMessages.add("音声ファイルの拡張子が違います");
        }
        if( !(textExtensions.equals("txt") || textExtensions.equals("TXT"))){
            errorMessages.add("テキストファイルの拡張子が違います");
        }
        return errorMessages;
    }

    /**
     *  リクエストパラメータで受け取った二つの共通したファイル名(拡張子以外)を抽出
     *
     * @param waveFilename, textFilename
     * @return String
     */
    public String extractCommonFileName(String waveFilename, String textFilename){
        String waveExtensions = this.getExtension(waveFilename);
        String textExtensions = this.getExtension(textFilename);

        String waveFilenameNoExtensions = waveFilename.replace("." + waveExtensions, "");
        String textFilenameNoExtensions = textFilename.replace("." + textExtensions, "");

        if(waveFilenameNoExtensions.equals(textFilenameNoExtensions)){
            // 共通していれば、それを返す
            return waveFilenameNoExtensions;
        }
        return null;

    }

    /**
     *  母音が正しい種類かをチェック
     *
     * @param vowel
     * @return String
     */
    public String checkTypeOfVowel(String vowel){
        List<String> vowelList = Arrays.asList("a", "i", "u", "e", "o");

        if(vowelList.contains(vowel)){
            return null;
        }
        return "正しい母音の種類を選択してください";
    }

    /**
     *  音声ファイル書き出し
     *
     * @param waveByte, filepath
     * @return void
     */
    public void writeWaveData(byte[] waveByte, String filepath) throws IOException {
        AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
        AudioInputStream audioInputStream = new AudioInputStream(
                new ByteArrayInputStream(waveByte)
                , audioFormat
                , waveByte.length);
        AudioSystem.write(
                audioInputStream
                , AudioFileFormat.Type.WAVE
                , new File(filepath));
    }

    /**
     *  テキストファイル書き出し
     *
     * @param textdata, filepath
     * @return void
     */
    public void writeTextData(String textdata, String filepath) throws IOException {
        FileWriter filewriter = new FileWriter(filepath,false);
        filewriter.write(textdata);
        filewriter.close();
    }


    /**
     * 音素セグメンテーションが実装された「segmentation-kit」をラップしたもの
     *
     * @param  nameOfDirForSegmentation 音声ファイルとテキストファイルを保存する作業用ディレクトリ
     * @return
     */
    public Integer wrapperOfSegmentationKit(String nameOfDirForSegmentation) throws InterruptedException, IOException {

        // 音素セグメンテーション実行
        String[] Command = { "/usr/local/bin/perl", "segment_julius.pl", nameOfDirForSegmentation};         // コマンド内容
        ProcessBuilder processBuilder = new ProcessBuilder(Command);
        File dir = new File("library/segmentation-kit");                          // 作業カレントディレクトリの指定
        processBuilder.directory(dir);
        Process process = processBuilder.start();                                           // コマンド実行
        return process.waitFor();                                                           // 終了したならば0

    }

    /**
     * 音声ファイルから母音部分を抽出し,母音保存用ディレクトリに保存
     *
     * @param  nameOfDirForSegmentation 音声ファイルとlabファイルがあるディレクトリ,
     *         commonFilename           拡張子を除くファイル名
     *         nameOfDirForVowel        母音を保存するディレクトリ
     *         vowel                    抽出対象の母音
     * @return
     */
    public Integer vowelExtraction(String nameOfDirForSegmentation, String commonFilename, String nameOfDirForVowel, String vowel) throws IOException, InterruptedException {

        String[] Command = { "/usr/local/bin/python3", "vowel_extraction.py", nameOfDirForSegmentation, commonFilename, nameOfDirForVowel, vowel};      // コマンド内容
        ProcessBuilder processBuilder = new ProcessBuilder(Command);
        File dir = new File("library/python");                                    // 作業カレントディレクトリの指定
        processBuilder.directory(dir);
        Process process = processBuilder.start();                                           // コマンド実行
        return process.waitFor();                                                           // 終了したならば0

    }

    /**
     * 権限の付与
     *
     * @param  nameOfDirForSegmentation 音声ファイルとlabファイルがあるディレクトリ,
     *
     * @return
     */
    public Integer setPermission(String nameOfDirForSegmentation) throws IOException, InterruptedException {

        String[] Command = { "chmod", "-R", "777", nameOfDirForSegmentation};      // コマンド内容
        ProcessBuilder processBuilder = new ProcessBuilder(Command);
        Process process = processBuilder.start();                                           // コマンド実行
        return process.waitFor();                                                           // 終了したならば0

    }
}
