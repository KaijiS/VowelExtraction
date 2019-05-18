package com.kaijis.VowelExtraction.model.Service;

import com.kaijis.VowelExtraction.model.Repository.VowelsRepository;
import com.kaijis.VowelExtraction.model.entity.Vowels;
import com.kaijis.VowelExtraction.model.mybatis.mapper.VowelsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

@Service
public class VowelsService {

    @Autowired
    private VowelsRepository vowelsRepository;

    @Autowired
    private VowelsMapper vowelsMapper;

    public List<Vowels> findByOriginalFileId(Long originalFileId) {
        return vowelsRepository.findByOriginalFileId(originalFileId);
    }

    public Vowels save(Vowels vowel) {
        return vowelsRepository.save(vowel);
    }

    public void deleteByOriginalFileId(Long originalFileId) {
        vowelsMapper.deleteByOriginalFileId(originalFileId);
    }

    /**
     *  母音ファイルを取得しbase64へ変換
     */
    public String getVowelFileBase64(String filepath) throws IOException {
        File wavefile = new File(filepath);
        byte[] bytes = Files.readAllBytes(wavefile.toPath());
        return Base64.getEncoder().encodeToString(bytes); // base64へ変換
    }

}
