#!/usr/bin/python
# coding:utf-8

import wave
import numpy as np
import scipy.signal
import array
import sys
import os

"""
使用方法

python  vowel_extraction.py [音声ファイルとlabファイルがあるディレクトリ名] [拡張子を除く音声ファイル名] [抽出した母音音声ファイルを保存する一時ディレクトリ名] [抽出対象の母音("a"~"o")]
  or
python3 vowel_extraction.py [音声ファイルとlabファイルがあるディレクトリ名] [拡張子を除く音声ファイル名] [抽出した母音音声ファイルを保存する一時ディレクトリ名] [抽出対象の母音("a"~"o")]
"""


class VowelExtraction:
    """
    クラス変数
    -----------
    self.__common_filename  : string        拡張子を除く音声ファイル名
    self.__filepath_wave    : string        音声ファイルのpath
    self.__filepath_lab     : string        labファイルのpath
    self.__output_dir       : string        抽出した母音音声ファイルを保存する一時ディレクトリ名
    self.__signal           : numpy_1d_data 音声ファイルから得られたの時系列データ shape(n_length,)
    self.__fs               : int           音声ファイルから得られた音声のサンプリング周波数
    self.__time             : numpy_1d_data 音声ファイルから得られたの時系列データの時間軸 shape(n_length,)
    self.__lab_data         : string        labファイルの中身
    """

    def __init__(self, wavefile_lab_dir, common_filename, output_dir):
        """コンストラクタ"""
        self.__common_filename = common_filename
        self.__filepath_wave = wavefile_lab_dir +'/'+common_filename+'.wav'
        self._read_wave()
        self.__filepath_lab = wavefile_lab_dir +'/'+common_filename+'.lab'
        self._read_lab()
        self.__output_dir = output_dir


    
    def _read_wave(self):
        """
        音声ファイルを読み込み時系列データの作成(適宜調整)
        """
        wf = wave.open(self.__filepath_wave, "rb")
        fs = wf.getframerate() #16000Hz
        #getnframes > 全サンプル, readframes > 指定した長さのフレーム
        x = wf.readframes(wf.getnframes())
        #frombuffer > バイナリ表記をintに変換
        x = np.frombuffer(x, dtype="int16") #/ 32768.0 #(-1, 1)に正規化
        wf.close()

        self.__signal = x
        self.__fs     = float(fs)
        self.__time   = np.arange(0.0, len(self.__signal)/self.__fs, 1/self.__fs)
    

    def _read_lab(self):
        """
        labファイル読み込み
        """
        f = open(self.__filepath_lab)
        self.__lab_data = f.read().rstrip()  # ファイル終端まで全て読んだデータを返す
        f.close()


    def _normalize(self, signal):
        """
        音声の正規化(使用してないが、一応)
        """

        if self.__signal == None:
            return

        sMax = self.__signal.max()
        sMin = np.abs(self.__signal.min())
        if sMax < sMin:
            sMax = sMin
        self.__signal = self.__signal / sMax
        return self.__signal
    

    def _extract_and_save_vowel(self, vowel):
        """
        母音を抽出して保存
        
        Parameters
        --------------
        vowel   string  対象の母音
        """

        # 文字列の情報を2重リストへ変換(行:一つの音素, 列:音素の開始時間,終了時間,音素といったの情報)
        phonemes_times = self.__lab_data.split('\n') # 改行で区切り、それぞれリストの要素とする
        for idx, phoneme_time in enumerate(phonemes_times):
            phonemes_times[idx] = phoneme_time.split(' ') # スペースで区切り、それぞれリストの要素とする

        for idx,phoneme_time in enumerate(phonemes_times):
            # 母音の前の子音を取得
            if idx == 0:
                consonant = "non"
            else:
                consonant = phonemes_times[idx-1][2]

            # 対象母音の音素のみ抽出
            if vowel == phoneme_time[2]:
                # 対象の波形を切り出し
                cut_wav = self.__signal[int(float(phoneme_time[0])*self.__fs):int(float(phoneme_time[1])*self.__fs)+1]
                # wavファイルに変換し保存
                filename = self.__common_filename + "_" + consonant + "_" + vowel + "_" + str(int(float(phoneme_time[0])*(10**6))) + "us.wav"
                self._create_and_save_wavefile(filename,cut_wav)

    
    def _create_and_save_wavefile(self, filename, data):
        """
        抽出された母音部分の音声ファイルを生成し保存
        
        Parameters
        --------------
        filename    string          保存するファイル名
        data        numpy_1d_array  抽出した母音時系列データ
        """
        filepath = self.__output_dir + "/" + filename

        # save wav file
        buf = data
        w = wave.Wave_write(filepath)
        w.setparams((
            1,                        # channel
            2,                        # byte width
            self.__fs,                # sampling rate
            len(buf),                 # number of frames
            "NONE", "not compressed"  # no compression
        ))
        # w.writeframes(array.array('h', buf).tostring())
        w.writeframes(array.array('h', buf).tobytes())
        w.close()



def main():
    # コマンドライン引数を受け取り
    WAVEFILE_LAB_DIR    = sys.argv[1]
    common_filename     = sys.argv[2]
    OUTPUT_DIR          = sys.argv[3]
    vowel               = sys.argv[4]


    vowel_extraction = VowelExtraction(wavefile_lab_dir = WAVEFILE_LAB_DIR,
                                       common_filename  = common_filename,
                                       output_dir       = OUTPUT_DIR)

    vowel_extraction._extract_and_save_vowel(vowel)


if __name__ == '__main__':
    main()