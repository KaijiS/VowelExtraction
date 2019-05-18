package com.kaijis.VowelExtraction.model.mybatis.entity;

import lombok.Data;


@Data
public class OriginalFilesEntity {

    private Long    id;

    private String  filename;

    private Long    user_id;

    private String  file_dir;

    private String  upload_at;

}
