package com.kaijis.VowelExtraction.model.entity;

import lombok.Data;

import javax.persistence.*;


@Entity                         // Entity生成
@Data                        // getterとsetterを勝手にやってくれる
@Table(name="vowels")           // テーブル名指定
public class Vowels {

    @Id // 以下のカラムを主キー設定
    @GeneratedValue(strategy = GenerationType.IDENTITY) // オートインクリメント
    @Column(name = "id")
    private Long id;

    @Column(name = "original_file_id")
    private Long originalFileId;

    @Column(name = "filename")
    private String filename;

    @Column(name = "filepath")
    private String filepath;

}