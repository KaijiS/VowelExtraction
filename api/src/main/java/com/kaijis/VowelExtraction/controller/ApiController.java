package com.kaijis.VowelExtraction.controller;


import com.kaijis.VowelExtraction.controller.request.originalfile.RequestOriginalFile;
import com.kaijis.VowelExtraction.controller.response.error.ResponseError;
import com.kaijis.VowelExtraction.controller.response.originalfiles.ResponseCount;
import com.kaijis.VowelExtraction.controller.response.originalfiles.ResponseOriginalFilesList;
import com.kaijis.VowelExtraction.controller.response.originalfiles.ResponseVowelInfo;
import com.kaijis.VowelExtraction.controller.response.originalfiles.ResponseVowelsList;
import com.kaijis.VowelExtraction.library.Wave;
import com.kaijis.VowelExtraction.model.Service.OriginalFilesService;
import com.kaijis.VowelExtraction.model.Service.UsersService;
import com.kaijis.VowelExtraction.model.Service.VowelsService;
import com.kaijis.VowelExtraction.model.entity.OriginalFiles;
import com.kaijis.VowelExtraction.model.entity.Vowels;
import com.kaijis.VowelExtraction.model.mybatis.entity.OriginalFilesEntity;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author sugimotokaiji
 */
@Api(tags = "api for vowel extraction")
@RestController
@CrossOrigin
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private final UsersService          usersService;
    private final OriginalFilesService  originalFilesService;
    private final VowelsService         vowelsService;

    @Autowired
    MessageSource messagesource;

    public ApiController(UsersService usersService, OriginalFilesService originalFilesService, VowelsService vowelsService) {
        this.usersService =         usersService;
        this.originalFilesService = originalFilesService;
        this.vowelsService =        vowelsService;
    }


    /**
     *  その人のデータ数を返す
     *
     * @param headerToken
     * @return json
     */
    @ApiOperation(value = "ユーザが処理したデータ数を取得")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ResponseCount.class),
            @ApiResponse(code = 403, message = "FORBIDDEN", response = ResponseError.class),
            @ApiResponse(code = 408, message = "REQUEST_TIMEOUT", response = ResponseError.class),
    })
    @GetMapping("/count")
    public ResponseEntity<Object> count(@ApiParam(value = "Token", required=true) @RequestHeader("Authorization") String headerToken){

        // そのトークンをもつユーザが存在するか
        Long userId = usersService.checkUser_and_getId(headerToken);
        if(userId == null){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error403",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        // トークンの有効期限はどうか
        Object checkExpirationDate_objectType = usersService.check_expiration_date(userId);
        if (checkExpirationDate_objectType == null || !(boolean)checkExpirationDate_objectType){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error408",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.REQUEST_TIMEOUT);
        }

        Integer count = originalFilesService.countByUserId(userId);

        ResponseCount response = new ResponseCount();
        response.setCount(count);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }



    /**
     *  その人のデータ一覧取得(全件 or ページごと)
     *
     * @param headerToken
     * @return json
     */
    @ApiOperation(value = "ユーザが処理したデータを取得")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ResponseOriginalFilesList.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "FORBIDDEN", response = ResponseError.class),
            @ApiResponse(code = 408, message = "REQUEST_TIMEOUT", response = ResponseError.class),
    })
    @GetMapping
    public ResponseEntity<Object> index(@ApiParam(value = "Token", required=true) @RequestHeader("Authorization") String headerToken, @ApiParam(value = "ページ番号") @RequestParam(name = "page", required = false) Integer page, @ApiParam(value = "1ページあたりのデータ数") @RequestParam(name = "numberOfDataPerPage", required = false) Integer numberOfDataPerPage) {

        // そのトークンをもつユーザが存在するか
        Long userId = usersService.checkUser_and_getId(headerToken);
        if(userId == null){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error403",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        // トークンの有効期限はどうか
        Object checkExpirationDate_objectType = usersService.check_expiration_date(userId);
        if (checkExpirationDate_objectType == null || !(boolean)checkExpirationDate_objectType){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error408",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.REQUEST_TIMEOUT);
        }

        if(page == null) {
            // 全件取得する場合
            List<OriginalFiles> originalFilesList = originalFilesService.findByUserIdOrderByUploadAtDesc(userId);

            List<ResponseOriginalFilesList> response = new ArrayList<ResponseOriginalFilesList>();
            for (OriginalFiles originalFiles : originalFilesList) {
                ResponseOriginalFilesList partOfResponse = new ResponseOriginalFilesList();
                partOfResponse.setId(originalFiles.getId());
                partOfResponse.setFilename(originalFiles.getFilename());
                partOfResponse.setUploadAt(originalFiles.getUploadAt());
                response.add(partOfResponse);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);

        } else {
            // ページごとに取得する場合
            List<OriginalFilesEntity> originalFilesEntityList = originalFilesService.findByUserIdOrderByUploadAtDescLimitOffset(userId, (page-1)*numberOfDataPerPage, numberOfDataPerPage);

            List<ResponseOriginalFilesList> response = new ArrayList<ResponseOriginalFilesList>();
            for (OriginalFilesEntity originalFilesEntity : originalFilesEntityList) {
                ResponseOriginalFilesList partOfResponse = new ResponseOriginalFilesList();
                partOfResponse.setId(originalFilesEntity.getId());
                partOfResponse.setFilename(originalFilesEntity.getFilename());
                partOfResponse.setUploadAt(originalFilesEntity.getUpload_at());
                response.add(partOfResponse);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        }



    }

    /**
     *  その人のデータ(id)の情報とその母音情報を取得
     *
     * @param headerToken, id
     * @return json
     */
    @ApiOperation(value = "ユーザが処理したデータ(id)の情報とその母音情報を取得")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ResponseVowelsList.class),
            @ApiResponse(code = 403, message = "FORBIDDEN", response = ResponseError.class),
            @ApiResponse(code = 404, message = "NOT_FOUND)", response = ResponseError.class),
            @ApiResponse(code = 408, message = "REQUEST_TIMEOUT", response = ResponseError.class),
            @ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = ResponseError.class),
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> show(@ApiParam(value = "Token", required=true) @RequestHeader("Authorization") String headerToken, @ApiParam(value = "original_data_id", required=true) @PathVariable long id) {

        // そのトークンをもつユーザが存在するか
        Long userId = usersService.checkUser_and_getId(headerToken);
        if(userId == null){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error403",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        // トークンの有効期限はどうか
        Object checkExpirationDate_objectType = usersService.check_expiration_date(userId);
        if (checkExpirationDate_objectType == null || !(boolean)checkExpirationDate_objectType){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error408",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.REQUEST_TIMEOUT);
        }

        // idのデータを取得
        Optional<OriginalFiles> originalFile_optional = originalFilesService.findById(id);

        if(!originalFile_optional.isPresent()){
            // ない場合はその旨をjsonで返す
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error404_id",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        OriginalFiles originalFile = originalFile_optional.get();

        if(originalFile.getUserId() != userId){
            // 該当したユーザ以外が参照してはいけない
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error403_show",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }


        Long originalFileId = id;
        List<Vowels> vowels = vowelsService.findByOriginalFileId(originalFileId);

        ResponseVowelsList response = new ResponseVowelsList();
        response.setId(originalFile.getId());
        response.setFilename(originalFile.getFilename());
        response.setUploadAt(originalFile.getUploadAt());


        String filepathBase = originalFile.getFileDir() + "/" + originalFile.getFilename();
        try {
            // 音声ファイルについて
            String wavedata = originalFilesService.getWaveFileBase64(filepathBase + ".wav");

            // テキストファイル について
            String textdata = originalFilesService.readFile(filepathBase + ".txt", false);

            // labファイルについて
            String labdata = originalFilesService.readFile(filepathBase + ".lab", true);

            // labファイルについて
            String logdata = originalFilesService.readFile(filepathBase + ".log", true);

            response.setWavefile(wavedata);
            response.setTextfile(textdata);
            response.setLabfile(labdata);
            response.setLogfile(logdata);

            for (Vowels vowel : vowels) {
                ResponseVowelInfo voweldata = new ResponseVowelInfo();

                // 母音ファイル読み込み
                String vowel64 = vowelsService.getVowelFileBase64(vowel.getFilepath());

                voweldata.setFilename(new File(vowel.getFilepath()).getName());
                voweldata.setWavedata(vowel64);
                response.getVoweldata().add(voweldata);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);

        }catch(Exception e){
            ResponseError responseError = new ResponseError();
            responseError.getMessages().add(messagesource.getMessage("error500",null, Locale.JAPAN));
            responseError.getMessages().add(e.getMessage());
            return new ResponseEntity<>(responseError, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     *  母音抽出の実行とデータの保存
     *
     * @param headerToken, requestOriginalFile
     * @return json
     */
    @ApiOperation(value = "母音抽出の実行とデータの保存")
    @ApiResponses({
            @ApiResponse(code = 201, message = "CREATED", response = ResponseVowelsList.class),
            @ApiResponse(code = 403, message = "FORBIDDEN", response = ResponseError.class),
            @ApiResponse(code = 408, message = "REQUEST_TIMEOUT", response = ResponseError.class),
            @ApiResponse(code = 415, message = "UNSUPPORTED_MEDIA_TYPE", response = ResponseError.class),
            @ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = ResponseError.class),
    })
    @PostMapping
    public ResponseEntity<Object> add(@ApiParam(value = "original_file", required=true) @RequestBody RequestOriginalFile requestOriginalFile, @ApiParam(value = "Token", required=true) @RequestHeader("Authorization") String headerToken) {

        // そのトークンをもつユーザが存在するか
        Long userId = usersService.checkUser_and_getId(headerToken);
        if(userId == null){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error403",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        // トークンの有効期限はどうか
        Object checkExpirationDate_objectType = usersService.check_expiration_date(userId);
        if (checkExpirationDate_objectType == null || !(boolean)checkExpirationDate_objectType){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error408",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.REQUEST_TIMEOUT);
        }


        String wavedata         = requestOriginalFile.getWavedata();
        String waveFilename     = requestOriginalFile.getWaveFilename();
        String textdata         = requestOriginalFile.getTextdata();
        String textFilename     = requestOriginalFile.getTextFilename();
        String vowel            = requestOriginalFile.getVowel();


        List<String> errorMessages = new ArrayList<String>();

        // 拡張子のチェック
        errorMessages.addAll(originalFilesService.checkExtension(waveFilename, textFilename));

        // 拡張子以外の共通しているファイルネームを抽出
        String commonFilename = originalFilesService.extractCommonFileName(waveFilename, textFilename);
        if(StringUtils.isEmpty(commonFilename)){
            errorMessages.add("ファイル名を統一してください");
        }

        // 母音の種類が正しいかチェック
        String vowelErrorMessage = originalFilesService.checkTypeOfVowel(vowel);
        if(!StringUtils.isEmpty(vowelErrorMessage)){
            errorMessages.add(vowelErrorMessage);
        }

        // waveファイルの情報チェック
        String base64 = wavedata.replaceFirst("data:.*;base64,", ""); // wavedata(base64)から"data:audio/wave;base64,"を削除する
        byte[] waveByte = Base64.getDecoder().decode(base64);
        Wave waveDataInfo = new Wave(waveByte);
        List<String> waveErrorMessages = waveDataInfo.check();
        errorMessages.addAll(waveErrorMessages);

        // 一つでもエラーメッセージがあるとその内容を返す
        if(errorMessages.size() > 0){
            ResponseError response = new ResponseError();
            response.getMessages().addAll(errorMessages);
            return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }


        // セグメンテーション実行作業用のディレクトリをに保存

        // まずは保存先ディレクトリ名指定
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String timestampToString = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(timestamp);
        String nameOfDirForSegmentation = System.getProperty("user.dir") + "/wavefiles/filesForSegmentation/" + userId + "/" +timestampToString; // 保存ディレクトリ名
        File dirForSegmentation = new File(nameOfDirForSegmentation);
        if (!dirForSegmentation.exists()) {
            dirForSegmentation.mkdirs();
        }

        try {
            // 音声ファイルの保存
            originalFilesService.writeWaveData(waveByte, nameOfDirForSegmentation + "/" + commonFilename + ".wav");

            // テキストファイルの書き出し
            originalFilesService.writeTextData(textdata, nameOfDirForSegmentation + "/" + commonFilename + ".txt");

        } catch(IOException e){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error500",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }


        // 音素セグメンテーションの実行
        try{
            // 作業用ディレクトリ(filesForSegmentation)の権限付与
            originalFilesService.setPermission("wavefiles/fileForSegmentation");
            // 実行プログラムの権限も付与
            originalFilesService.setPermission("library");

            // 音素セグメンテーション
            originalFilesService.wrapperOfSegmentationKit(nameOfDirForSegmentation);

        }catch(Exception e){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error500",null, Locale.JAPAN));
            response.getMessages().add(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 現在時刻を取得
        Date date_for_onetime = new Date();                                 // 現在の時刻を取得
        Calendar date_typeCalendar_for_onetime = Calendar.getInstance();    // インスタンス生成
        date_typeCalendar_for_onetime.setTime(date_for_onetime);            // そのインスタンスに現在時刻をセット
        // 文字列に変換
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // フォーマットの定義
        String uploadAt = sdf.format(date_typeCalendar_for_onetime.getTime());


        // wav txt lab logファイルの共通の情報についてデータベースへ保存するための情報整理とデータベースへの保存
        OriginalFiles originalFile     = new OriginalFiles();
        originalFile.setFilename(commonFilename);
        originalFile.setUserId(userId);
        originalFile.setFileDir(nameOfDirForSegmentation);
        originalFile.setUploadAt(uploadAt);
        originalFilesService.save(originalFile);

        // 抽出された母音を保存するためのディレクトリを準備
        String nameOfDirForVowel = System.getProperty("user.dir") + "/wavefiles/vowels/" + userId + "/" + originalFile.getId(); // 保存ディレクトリ名
        File dirForVowel = new File(nameOfDirForVowel);
        if (!dirForVowel.exists()) {
            dirForVowel.mkdirs();
        }


        // 対象の母音の波形を抽出し、音声ファイルへ変換して保存
        try{
            // vowelsディレクトリの権限付与
            originalFilesService.setPermission("wavefiles/vowels");

            // 母音抽出
            originalFilesService.vowelExtraction(nameOfDirForSegmentation, commonFilename, nameOfDirForVowel, vowel);

        }catch(Exception e){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error500",null, Locale.JAPAN));
            response.getMessages().add(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // それらの情報をデータベースに保存する
        File[] vowelfiles = dirForVowel.listFiles();
        for (File vowelfile : vowelfiles) {
            Vowels vowelInfo = new Vowels();
            vowelInfo.setOriginalFileId(originalFile.getId());
            vowelInfo.setFilename(vowelfile.getName());
            vowelInfo.setFilepath(vowelfile.getPath());
            vowelsService.save(vowelInfo);
        }


        ResponseVowelsList response = new ResponseVowelsList();
        response.setId(originalFile.getId());
        response.setFilename(originalFile.getFilename());

        String filepathBase = originalFile.getFileDir() + "/" + originalFile.getFilename();
        try {
            // labファイルについて
            String labdata = originalFilesService.readFile(filepathBase + ".lab", true);
            response.setLabfile(labdata);


            for (File vowelfile : vowelfiles) {
                ResponseVowelInfo voweldata = new ResponseVowelInfo();

                // 母音ファイル読み込み
                String vowel64 = vowelsService.getVowelFileBase64(vowelfile.toPath().toString());

                voweldata.setFilename(vowelfile.getName());
                voweldata.setWavedata(vowel64);
                response.getVoweldata().add(voweldata);
            }

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        }catch(Exception e){
            ResponseError responseError = new ResponseError();
            responseError.getMessages().add(messagesource.getMessage("error500",null, Locale.JAPAN));
            responseError.getMessages().add(e.getMessage());
            return new ResponseEntity<>(responseError, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }



    /**
     *  母音のデータとその抽出元のデータの削除
     *
     * @param headerToken, requestOriginalFile
     * @return json
     */
    @ApiOperation(value = "母音のデータとその抽出元のデータの削除")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "FORBIDDEN", response = ResponseError.class),
            @ApiResponse(code = 404, message = "NOT_FOUND", response = ResponseError.class),
            @ApiResponse(code = 408, message = "REQUEST_TIMEOUT", response = ResponseError.class),
            @ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = ResponseError.class),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@ApiParam(value = "original_fi;e_id", required=true) @PathVariable long id, @ApiParam(value = "Token", required=true)  @RequestHeader("Authorization") String headerToken) {

        // そのトークンをもつユーザが存在するか
        Long userId = usersService.checkUser_and_getId(headerToken);
        if(userId == null){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error403",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        // トークンの有効期限はどうか
        Object checkExpirationDate_objectType = usersService.check_expiration_date(userId);
        if (checkExpirationDate_objectType == null || !(boolean)checkExpirationDate_objectType){
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error408",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.REQUEST_TIMEOUT);
        }

        // idのデータを取得
        Optional<OriginalFiles> originalFile_optional = originalFilesService.findById(id);

        if(!originalFile_optional.isPresent()){
            // ない場合はその旨をjsonで返す
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error404_id",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        OriginalFiles originalFile = originalFile_optional.get();

        if(originalFile.getUserId() != userId){
            // 該当したユーザ以外は削除できない
            ResponseError response = new ResponseError();
            response.getMessages().add(messagesource.getMessage("error403_delete",null, Locale.JAPAN));
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }


        // 保存している母音ファイルを削除するための準備(ファイルパスを取得)
        Long originalFileId = id;
        // 抽出元のファイルがあるディレクトリの取得
        String dirOfOriginalFiles = originalFile.getFileDir();
        // 母音ファイルのファイルパスの取得
        List<Vowels> vowels = vowelsService.findByOriginalFileId(originalFileId);
        List<String> vowelsFilepaths = new ArrayList<String>();
        for(Vowels vowel : vowels ){
            vowelsFilepaths.add(vowel.getFilepath());
        }

        // データベースからの削除を行う
        vowelsService.deleteByOriginalFileId(originalFileId); // 母音
        originalFilesService.delete(originalFile);            // 抽出元のファイル

        // ファイルの削除
        // まずは母音の情報から削除
        for(String vowelFilepath : vowelsFilepaths) {
            File file = new File(vowelFilepath);
            if(!file.delete()){
                ResponseError response = new ResponseError();
                response.getMessages().add(messagesource.getMessage("error403_delete",null, Locale.JAPAN));
                response.getMessages().add("母音ファイルの削除が失敗しました");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        // その親ディレクトリも削除
        File parent = new File(vowelsFilepaths.get(0)).getParentFile();
        parent.delete();


        // 次に抽出元のファイルの削除
        File dir = new File(dirOfOriginalFiles);
        File[] filelist = dir.listFiles();
        for(File file : filelist){
            if(!file.delete()){
                ResponseError response = new ResponseError();
                response.getMessages().add(messagesource.getMessage("error403_delete",null, Locale.JAPAN));
                response.getMessages().add("オリジナルファイルの削除が失敗しました");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        dir.delete();

        HashMap<String, Object> json = new LinkedHashMap<>();
        List<String> messages = new ArrayList<String>();
        messages.add("削除完了");
        json.put("messages", messages);
        return new ResponseEntity<>(json, HttpStatus.OK);

    }


}
