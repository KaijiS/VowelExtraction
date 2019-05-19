package com.kaijis.VowelExtraction.controller;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.kaijis.VowelExtraction.config.WebFrontConfig;
import com.kaijis.VowelExtraction.controller.response.error.ResponseError;
import com.kaijis.VowelExtraction.controller.response.users.ResponseOAuthLogin;
import com.kaijis.VowelExtraction.controller.response.users.ResponseToken;
import com.kaijis.VowelExtraction.controller.response.users.ResponseUserInfo;
import com.kaijis.VowelExtraction.model.Service.OriginalFilesService;
import com.kaijis.VowelExtraction.model.Service.UsersService;
import com.kaijis.VowelExtraction.model.Service.VowelsService;
import com.kaijis.VowelExtraction.model.entity.OriginalFiles;
import com.kaijis.VowelExtraction.model.entity.Users;
import com.kaijis.VowelExtraction.model.entity.Vowels;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
//import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sugimotokaiji
 */
@Api(tags = "user")
@RestController
@CrossOrigin
@RequestMapping("/user")
public class UsersController {

  @Autowired
  private final UsersService         usersService;
  private final OriginalFilesService originalFilesService;
  private final VowelsService        vowelsService;

  @Autowired
  MessageSource messagesource;

  @Autowired
  private WebFrontConfig webFrontConfig;

  public UsersController(UsersService usersService, OriginalFilesService originalFilesService, VowelsService vowelsService) {
    this.usersService =         usersService;
    this.originalFilesService = originalFilesService;
    this.vowelsService =        vowelsService;
  }


  /**
   * 　ユーザ情報取得
   *
   * @param headerToken
   * @return json
   */
  @ApiOperation(value = "ユーザ情報を取得")
  @ApiResponses({
          @ApiResponse(code = 200, message = "OK", response = ResponseUserInfo.class),
          @ApiResponse(code = 403, message = "FORBIDDEN", response = ResponseError.class),
          @ApiResponse(code = 408, message = "REQUEST_TIMEOUT", response = ResponseError.class),
  })
  @GetMapping
  public ResponseEntity<Object> index(@ApiParam(value = "Token", required=true) @RequestHeader("Authorization") String headerToken) {

    // そのトークンをもつユーザが存在するか
    Object id_objectType = usersService.checkUser_and_getId(headerToken);
    if(id_objectType == null){
      ResponseError response = new ResponseError();
      response.getMessages().add(messagesource.getMessage("error403",null, Locale.JAPAN));
      return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    long id = (long)id_objectType;

    // トークンの有効期限はどうか
    Object checkExpirationDate_objectType = usersService.check_expiration_date(id);
    if (checkExpirationDate_objectType == null || !(boolean)checkExpirationDate_objectType){
      ResponseError response = new ResponseError();
      response.getMessages().add(messagesource.getMessage("error408",null, Locale.JAPAN));
      return new ResponseEntity<>(response, HttpStatus.REQUEST_TIMEOUT);
    }

    // そのユーザの基本情報をユーザテーブルから取得
    Optional<Users> user_info_optional = usersService.findById(id);
    Users userInfo = user_info_optional.get();

    ResponseUserInfo response = new ResponseUserInfo();
    response.setName(userInfo.getName());
    response.setAvatarUrl(userInfo.getAvatarUrl());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }



  /**
   * 　oauthログイン
   *
   * @param
   * @return json
   */
  @ApiOperation(value = "oauthログイン")
  @ApiResponses({
          @ApiResponse(code = 200, message = "OK", response = ResponseOAuthLogin.class),
  })
  @GetMapping(value = "/githuboauthlogin")
  public ResponseEntity<Object> githubOauthLogin() {

    OAuth20Service service = usersService.githubProvider();

    String authUrl = service.getAuthorizationUrl();

    ResponseOAuthLogin response = new ResponseOAuthLogin();
    response.setAuthUrl(authUrl);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


  /**
   * 　コールバックされたあとトークンを生成 ワンタイムトークンをフロントに返す
   *
   * @param code リクエストパラメータのcode
   * @return json or String tokenが欲しいクライアント先にリダイレクト
   */
  @ApiOperation(value = "コールバックされたあとトークンを生成 ワンタイムトークンをフロントに返す")
  @GetMapping(value = "/callback")
  public void callbackGithub(@ApiParam(value = "code", required=true) @RequestParam("code") String code, HttpServletResponse responseForRedirect) throws IOException {

    // トークンを生成
    String token                = usersService.createToken();
    // 有効期限を60分後
    String expiration_time_str  = usersService.setExpirationTime(60);

    // ワンタイムトークンを生成
    String onetimetoken         = usersService.createOneTimeToken();
    // その有効期限を2分後
    String onetime_str          = usersService.setExpirationTimeOfOneTimeToken(2);


    Map userInfo; // ユーザ情報受け取り用変数

    try {
      // アクセストークンを取得
      OAuth20Service service = usersService.githubProvider();
      OAuth2AccessToken accessToken = service.getAccessToken(code);

      // アクセストークンを用いてユーザ情報を取得
      OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
      service.signRequest(accessToken, request);
      Response response = service.execute(request);
      String userInfoJson = response.getBody();               // json(string)を取得
      userInfo = usersService.json2map(userInfoJson);         // json(string)をMap型に変換


    } catch (Exception e) {
      ResponseError response = new ResponseError();
      response.getMessages().add(messagesource.getMessage("error500",null, Locale.JAPAN));
      response.getMessages().add(e.getMessage());
      return;
    }


    // githubのidでuserテーブル内を検索する
    Optional<Users> existence_user_optional = usersService.findByOauthId(Long.parseLong(userInfo.get("id").toString()));
    if (!existence_user_optional.isPresent()) {
      Users newUser = new Users();
      newUser.setName(userInfo.get("login").toString());
      newUser.setOauthName("github");
      newUser.setOauthId(Long.parseLong(userInfo.get("id").toString()));
      newUser.setAvatarUrl(userInfo.get("avatar_url").toString());
      newUser.setToken(token);
      newUser.setExpirationTime(expiration_time_str);
      newUser.setOneTimeToken(onetimetoken);
      newUser.setExpirationTimeOfOneTime(onetime_str);
      usersService.save(newUser);
      responseForRedirect.sendRedirect(webFrontConfig.getOneTimeTokenUrl() + onetimetoken);
      return;
    }

    Users existence_user = existence_user_optional.get();
    existence_user.setName(userInfo.get("login").toString());
    existence_user.setToken(token);
    existence_user.setExpirationTime(expiration_time_str);
    existence_user.setOneTimeToken(onetimetoken);
    existence_user.setExpirationTimeOfOneTime(onetime_str);
    usersService.save(existence_user);
    responseForRedirect.sendRedirect(webFrontConfig.getOneTimeTokenUrl() + onetimetoken);
    return;

  }


  /**
   * 　ワンタイムトークンを受け取りトークンを返す
   *
   * @param onetimetoken
   * @return json token
   */
  @ApiOperation(value = "ワンタイムトークンを受け取りトークンを返す")
  @ApiResponses({
          @ApiResponse(code = 200, message = "OK", response = ResponseToken.class),
          @ApiResponse(code = 403, message = "FORBIDDEN", response = ResponseError.class),
          @ApiResponse(code = 408, message = "FORBIDDEN", response = ResponseError.class),
          @ApiResponse(code = 500, message = "FORBIDDEN", response = ResponseError.class),
  })
  @GetMapping(value = "/getToken")
  public ResponseEntity<Object> getToken(@ApiParam(value = "One Time Token", required=true) @RequestParam("onetimetoken") String onetimetoken) {

    // ワンタイムトークンが登録されているユーザを検索
    Optional<Users> existence_user_optional = usersService.findByOneTimeToken(onetimetoken);
    // いなければその旨をjsonで返す
    if (!existence_user_optional.isPresent()) {
      ResponseError response = new ResponseError();
      response.getMessages().add(messagesource.getMessage("error403",null, Locale.JAPAN));
      return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    Users userInfo = existence_user_optional.get();

    // 有効期限の確認
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // フォーマットの定義
      Date expiration_time = sdf.parse(userInfo.getExpirationTimeOfOneTime());
      // 現在の時刻を取得
      Date now_time = new Date();
      // 比較して返す
      if(expiration_time.compareTo(now_time) == -1){
        ResponseError response = new ResponseError();
        response.getMessages().add(messagesource.getMessage("error408",null, Locale.JAPAN));
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
      }

    } catch (ParseException e) {
      ResponseError response = new ResponseError();
      response.getMessages().add(messagesource.getMessage("error500",null, Locale.JAPAN));
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // トークンを取得し、jsonで返す
    String token = userInfo.getToken();

    // ワンタイムトークンは削除
    userInfo.setOneTimeToken(null);
    userInfo.setExpirationTimeOfOneTime(null);
    // ログイン状態をtrueに
    userInfo.setLoginState(true);
    usersService.save(userInfo);

    ResponseToken response = new ResponseToken();
    response.setToken(token);
    return new ResponseEntity<>(response, HttpStatus.OK);

  }

  /**
   * 　oauthログアウト
   *
   * @param headerToken
   * @return json
   */
  @ApiOperation(value = "ログアウト")
  @ApiResponses({
          @ApiResponse(code = 200, message = "OK"),
          @ApiResponse(code = 403, message = "FORBIDDEN", response = ResponseError.class),
  })
  @PutMapping("/logout")
  public ResponseEntity<Object> logout(@ApiParam(value = "Token", required=true) @RequestHeader("Authorization") String headerToken) {

    // そのトークンをもつユーザが存在するか
    Long id = usersService.checkUser_and_getId(headerToken);
    if(id == null){
      ResponseError response = new ResponseError();
      response.getMessages().add(messagesource.getMessage("error403",null, Locale.JAPAN));
      return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // そのユーザの基本情報をユーザテーブルから取得
    Optional<Users> user_info_optional = usersService.findById(id);
    // トークンを破棄し、ログイン状態をfalseに変更
    Users userInfo = user_info_optional.get();
    userInfo.setToken(null);
    userInfo.setExpirationTime(null);
    userInfo.setLoginState(false);
    usersService.save(userInfo);

    HashMap<String, Object> json = new LinkedHashMap<>();
    return new ResponseEntity<>(json, HttpStatus.OK);
  }



  /**
   * ユーザ情報の削除
   *
   * @param headerToken
   * @return json
   */
  @ApiOperation(value = "ユーザ情報の削除")
  @ApiResponses({
          @ApiResponse(code = 200, message = "OK"),
          @ApiResponse(code = 403, message = "FORBIDDEN", response = ResponseError.class),
          @ApiResponse(code = 408, message = "FORBIDDEN", response = ResponseError.class),
          @ApiResponse(code = 500, message = "FORBIDDEN", response = ResponseError.class),
  })
  @DeleteMapping
  public ResponseEntity<Object> delete(@ApiParam(value = "Token", required=true) @RequestHeader("Authorization") String headerToken) {

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

    // そのユーザの基本情報をユーザテーブルから取得
    Optional<Users> user_info_optional = usersService.findById(userId);
    Users userInfo = user_info_optional.get();

    // まずはそのユーザの音声データを取得し、1個ずつ削除する
    List<OriginalFiles> originalFiles = originalFilesService.findByUserId(userId);
    for(OriginalFiles originalFile : originalFiles){
      // 保存している母音ファイルを削除するための準備(ファイルパスを取得)
      // 抽出元のファイルがあるディレクトリの取得
      String dirOfOriginalFiles = originalFile.getFileDir();
      // 母音ファイルのファイルパスの取得
      List<Vowels> vowels = vowelsService.findByOriginalFileId(originalFile.getId());
      List<String> vowelsFilepaths = new ArrayList<String>();
      for(Vowels vowel : vowels ){
        vowelsFilepaths.add(vowel.getFilepath());
      }

      // データベースからの削除を行う
      vowelsService.deleteByOriginalFileId(originalFile.getId()); // 母音
      originalFilesService.delete(originalFile);                  // 抽出元のファイル

      // ファイルの削除
      // まずは母音の情報から削除
      for(String vowelFilepath : vowelsFilepaths) {
        File file = new File(vowelFilepath);
        if(!file.delete()){
          ResponseError response = new ResponseError();
          response.getMessages().add(messagesource.getMessage("error500",null, Locale.JAPAN));
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
          response.getMessages().add(messagesource.getMessage("error500",null, Locale.JAPAN));
          response.getMessages().add("オリジナルファイルの削除が失敗しました");
          return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
      }
      dir.delete();
    }

    // ユーザディレクトリの削除
    File dir2types = new File("wavefiles");
    File[] dirlist = dir2types.listFiles();
    for(File dir1type : dirlist){
      File usersFilePath = new File(dir1type.getPath().toString() + "/" + userId);
      usersFilePath.delete();
    }

    // ユーザの削除
    usersService.delete(userInfo);

    HashMap<String, Object> json = new LinkedHashMap<>();
    List<String> messages = new ArrayList<String>();
    messages.add("削除しました");
    json.put("messages", messages);
    return new ResponseEntity<>(json, HttpStatus.OK);
  }
}

