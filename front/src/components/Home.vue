<template>
  <div>

    <div v-if= "token">

      <addComponent></addComponent>

      <table class="table table-hover bg-light">
        <thead>
          <tr>
            <th>ファイル名</th>
            <th>アップロード日</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, key, index) in data" :key="index" @click="select(item.id)">
            <th>{{ item.filename }}</th>
            <td>{{ item.uploadAt}}</td>
          </tr>
        </tbody>
      </table>

      <nav aria-label="ページネーション">
        <ul class="pagination">
          <li class="page-item" :class="{disabled : nowpage-1 <= 0}"><a class="page-link" href="" v-on:click="prev">前へ</a></li>
          <li v-for= "(value, index) in lastpage" :key='index' class="page-item" :class="{active : value==nowpage}"><a class="page-link" href="" v-on:click="move(value)">{{value}}</a></li>
          <li class="page-item" :class="{disabled : nowpage+1 > lastpage}"><a class="page-link" href="" v-on:click="next">次へ</a></li>
        </ul>
      </nav>

    </div>

    <div v-else>
      <b-alert show variant="info">
        <p class="h5">
          <a href="https://github.com/julius-speech/segmentation-kit" target="_blank">
          Juliusの音素ラベリング(音素セグメンテーション)
          </a>
          を利用してアップロードされた音声データから母音を抽出し、抽出された母音の音声ファイルをダウンロードすることができます。
        </p>
      </b-alert>    
      <informationComponent></informationComponent>
    </div>
  </div>
</template>

<script>
import AddComponent from './Add.vue'
import InformationComponent from './Information.vue'
export default {
  name: 'home',
  data () {
    return {
      token: this.$session.get("token"),
      OAuth: this.$session.get("OAuth"),
      allcount: 0,              // ユーザの全データ数
      nowpage:  1,              // 現在のページ
      lastpage: 0,              // 最大ページ数
      numberOfDataPerPage: 20,  // 1ページあたりの表示数
      data: [],
    }
  },
  created () {
    // データ数取得
    if(this.$session.get("token")){
      this.getCount()
      .then(() => {
        if (this.allcount==0){
          return;
        }
        // 最大ページ数の計算
        this.lastpage = Math.ceil(this.allcount / this.numberOfDataPerPage);
        // 指定されたページ番号の取得
        this.nowpage = Number(this.$route.query.page);
        // ページ番号が指定されていなければ1ページ目を指定
        if (isNaN(this.nowpage)){
          this.nowpage = 1; 
        }
        // ページ番号が範囲外ならエラー表示
        if(this.nowpage < 1 || this.lastpage < this.nowpage){
          this.$router.push({ path: "/"})
          window.location.reload();
          return;
        }
        this.getDataList()
      })
    }
  },
  methods: {
    getCount: function() {
      return new Promise((resolve, reject) => { 
        this.$axios.get('/api/count',{
          headers: {
            Authorization: `Bearer `+this.$session.get('token'),
          },
          data: {}
        })
        .then(response => {
          this.allcount = response.data.count;
          resolve()
        })
        .catch(e => {
          if(e.request.status == 403 || e.request.status == 408){
            this.mixin_logout();
          }else{
            this.error_messages = JSON.parse(e.request.response).messages;
          }
        });
      })
    },
    getDataList: function() {
      return new Promise((resolve, reject) => { 
        this.$axios.get('/api',{
          params: {
            page: this.nowpage,
            numberOfDataPerPage: this.numberOfDataPerPage,
          },
          headers: {
            Authorization: `Bearer `+this.$session.get('token'),
          },
          data: {}
        })
        .then(response => {
          this.data = response.data;
        })
        .catch(e => {
          if(e.request.status == 403 || e.request.status == 408){
            this.mixin_logout();
          }else{
            this.error_messages = JSON.parse(e.request.response).messages;
          }
        });
      })
    },
    select: function (id) {
      this.$router.push({ path: "/show/" + id})
    },
    move: function (page) {
      this.$router.push({ path: "/?page=" + page})
    },
    prev: function () {
      let prevpage = this.nowpage-1;
      this.$router.push({ path: "/?page=" + prevpage})
    },
    next: function () {
      let nextpage = this.nowpage+1;
      this.$router.push({ path: "/?page=" + nextpage})
    }
  },
  components: {
    addComponent: AddComponent,
    informationComponent: InformationComponent
  }
  

}
</script>

<style>

</style>