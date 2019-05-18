package com.kaijis.VowelExtraction.model.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.kaijis.VowelExtraction.config.OAuthConfig;
import com.kaijis.VowelExtraction.model.Repository.UsersRepository;
import com.kaijis.VowelExtraction.model.entity.Users;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OAuthConfig oauthConfig;

    public List<Users> findAll() {
        return usersRepository.findAll();
    }

    public Optional<Users> findById(Long id) {
        return usersRepository.findById(id);
    }

    public Optional<Users> findByName(String name) {
        return usersRepository.findByName(name);
    }

    public Optional<Users> findByOauthId(Long oauth_id) {
        return usersRepository.findByOauthId(oauth_id);
    }

    public Optional<Users> findByToken(String token) {
        return usersRepository.findByToken(token);
    }

    public Optional<Users> findByOneTimeToken(String onetimetoken) {
        return usersRepository.findByOneTimeToken(onetimetoken);
    }

    public Users save(Users user) {
        return usersRepository.save(user);
    }

    public void delete(Users user) {
        usersRepository.delete(user);
    }


    /**
     * githubのOauthする情報
     *
     * @param
     * @return service
     */
    public OAuth20Service githubProvider() {
        OAuth20Service service = new ServiceBuilder(oauthConfig.getGithubClientId())
                .apiSecret(oauthConfig.getGithubClientSecret())
                .callback(oauthConfig.getGithubCallbackURL())
                .build(GitHubApi.instance());

        return service;
    }

    /**
     * トークン生成
     */
    public String createToken() {
        UUID uuid = UUID.randomUUID();
        String uuid_token = uuid.toString();
        String random_str = RandomStringUtils.randomAlphanumeric(20);
        return uuid_token + random_str;
    }

    /**
     * トークンの有効期限決定
     */
    public String setExpirationTime(Integer addMinite){
        Date date = new Date();                                 // 現在の時刻を取得
        Calendar date_typeCalendar = Calendar.getInstance();    // 加算するため　インスタンス生成
        date_typeCalendar.setTime(date);                        // そのインスタンスに現在時刻をセット
        date_typeCalendar.add(Calendar.MINUTE, addMinite);      // 1時間加算
        // 文字列に変換
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // フォーマットの定義
        return sdf.format(date_typeCalendar.getTime());
    }

    /**
     * ワンタイムトークン生成
     */
    public String createOneTimeToken() {
        return RandomStringUtils.randomAlphanumeric(30);
    }

    /**
     * ワンタイムトークンの有効期限決定
     */
    public String setExpirationTimeOfOneTimeToken(Integer addMinite){
        Date date_for_onetime = new Date();                                 // 現在の時刻を取得
        Calendar date_typeCalendar_for_onetime = Calendar.getInstance();    // 加算するため　インスタンス生成
        date_typeCalendar_for_onetime.setTime(date_for_onetime);            // そのインスタンスに現在時刻をセット
        date_typeCalendar_for_onetime.add(Calendar.MINUTE, addMinite);      // 2分加算
        // 文字列に変換
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // フォーマットの定義
        return sdf.format(date_typeCalendar_for_onetime.getTime());
    }

    /**
     * ワンタイムトークンの有効期限決定
     */
    public Map json2map(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Map.class);    // json(string)をMap型に変換
    }


    /**
     * トークンと対応するユーザidを取得
     * @param   headerToken  ヘッダーについているトークン
     * @return  id なければ null
     */
    public Long checkUser_and_getId(String headerToken) {

        String token = headerToken.replace("Bearer ", "");

        // そのトークンが存在する情報を取得
        Optional<Users> user_info_optional = this.findByToken(token);

        // なければnullを
        if (!user_info_optional.isPresent()){
            return null;
        }

        // あれば,そのユーザIDを返す
        Users user_info = user_info_optional.get();
        return user_info.getId();
    }


    /**
     * ユーザidより有効期限の確認
     * @param   id  user_id
     * @return  boolean 有効期限切れならfalse エラーの場合null
     */
    public Object check_expiration_date(long id) {

        try{
            // そのユーザの基本情報をユーザテーブルから取得し、有効期限を取得
            Optional<Users> user_info_optional = this.findById(id);
            Users user_info = user_info_optional.get();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // フォーマットの定義
            Date expiration_date = sdf.parse(user_info.getExpirationTime());
            // 現在の時刻を取得
            Date now_time = new Date();
            // 比較して返す
            if(expiration_date.compareTo(now_time) == -1){
                return false;
            }
            return true;

        } catch (ParseException e) {
            return null;
        }

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
