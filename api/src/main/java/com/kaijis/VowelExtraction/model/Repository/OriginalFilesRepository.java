package com.kaijis.VowelExtraction.model.Repository;

import com.kaijis.VowelExtraction.model.entity.OriginalFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OriginalFilesRepository extends JpaRepository<OriginalFiles, Long> {

    Optional<OriginalFiles> findById(Long id);

    List<OriginalFiles> findByUserId(Long userId);

    List<OriginalFiles> findByUserIdOrderByUploadAtDesc(Long userId);

    Integer countByUserId(Long userId);

}
