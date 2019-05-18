<template>

<div class="exp">
    <div class="h3 mark">ファイルの条件</div>
    <span class="under bigfont">ファイル名を揃えた</span>以下の2つのファイルを選択する
    <ul>
    <li>wavファイル</li>
    <ul>
        <li>サンプリング周波数:16000Hz</li>
        <li>量子化bit: 16bit</li>
        <li>チャンネル数:1 (モノラルのみ)</li>
    </ul>
    <li>txtファイル</li>
    <ul>
        <li>内容：音声ファイルの内容の読みを平仮名で記載</li>
        <li>文字コード："UTF-8"</li>
        <li>注意：「今日は」→「きょーわ」，「室蘭へ」→「むろらんえ」</li>
    </ul>
    </ul>
</div>

</template>

<script>
</script>

<style>
.exp {
    padding: 0.5em 1em;
    margin: 2em 0;
    color: #232323;
    background: #fff8e8;
    border-left: solid 10px #ffc06e;
}

.under {
  background: linear-gradient(transparent 70%, #fda2b2 70%);
}

.bigfont {
  font-size: 20px;
}
</style>