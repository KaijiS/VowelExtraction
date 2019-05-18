package com.kaijis.VowelExtraction.model.mybatis.mapper;


import com.kaijis.VowelExtraction.model.mybatis.entity.OriginalFilesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OriginalFilesMapper {

    List<OriginalFilesEntity> findByUserIdOrderByUploadAtDescLimitOffset(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

}
