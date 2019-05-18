package com.kaijis.VowelExtraction.model.Repository;


import com.kaijis.VowelExtraction.model.entity.Vowels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VowelsRepository extends JpaRepository<Vowels, Long> {

    List<Vowels> findByOriginalFileId(Long originalFileId);

}
