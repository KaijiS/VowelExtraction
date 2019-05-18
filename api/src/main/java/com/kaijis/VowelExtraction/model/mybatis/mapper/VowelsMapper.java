package com.kaijis.VowelExtraction.model.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VowelsMapper {

    void deleteByOriginalFileId(@Param("originalFileId") Long originalFileId);

}
