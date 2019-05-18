package com.kaijis.VowelExtraction.model.entity;

import lombok.Data;

import javax.persistence.*;

@Entity                 // Entity生成
@Data                   // getterとsetterを勝手にやってくれる
@Table(name="users")    // テーブル名指定
public class Users {

    @Id // 以下のカラムを主キー設定
    @GeneratedValue(strategy = GenerationType.IDENTITY) // オートインクリメント
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "oauth_name")
    private String oauthName;

    @Column(name = "oauth_id")
    private Long oauthId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "token")
    private String token;

    @Column(name = "expiration_time")
    private String expirationTime;

    @Column(name = "one_time_token")
    private String oneTimeToken;

    @Column(name = "expiration_time_one_time")
    private String expirationTimeOfOneTime;

    @Column(name = "login_state")
    private boolean loginState;

}