package com.kaijis.VowelExtraction.model.entity;

import lombok.Data;

import javax.persistence.*;


@Entity                         // Entity生成
@Data                           // getterとsetterを勝手にやってくれる
@Table(name="original_files")   // テーブル名指定
public class OriginalFiles {

    @Id // 以下のカラムを主キー設定
    @GeneratedValue(strategy = GenerationType.IDENTITY) // オートインクリメント
    @Column(name = "id")
    private Long id;

    @Column(name = "filename")
    private String filename;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "file_dir")
    private String fileDir;

    @Column(name = "upload_at")
    private String uploadAt;

}