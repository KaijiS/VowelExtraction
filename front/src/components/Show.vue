<template>
  <div>

    <b-button v-on:click="back" variant="outline-primary">戻る</b-button>
    <div class="float-right">
      <b-button variant="outline-danger" v-b-modal.modal-delete>Delete this content</b-button>
    </div>

    <b-modal id="modal-delete" title="アカウントの削除" @ok="delete_of_data" ok-variant="danger" ok-title=削除>
            本当にアカウントを削除しますか？<br>
        </b-modal>

    <div v-for= "(message, key, index) in error_messages" :key="index">
      <b-alert show variant="danger">{{ message }}</b-alert>
    </div>

    <br>

    <div class="row">

    <div class="col-md-6 p-10 mb-10 bg-light text-dark">
      <h3>アップロードファイルと<br>音素ラベリング結果</h3>
      <br>
      <b>実行日</b>  {{ data.uploadAt }}<br>
      
      <br><br>
      <h6>{{ data.filename }}.wav</h6>
      <audio controls :src="'data:audio/wav;base64,' + data.wavefile"></audio>
      
      <br><br>
      <h6><a :href="iframe_of_textfile" :download="dowmload_textfilename">{{ data.filename }}.txt</a></h6>
      <iframe :src="iframe_of_textfile" width="330" >
        この部分はインラインフレームを使用
      </iframe>
      
      <br><br>
      <h6><a :href="iframe_of_labfile" :download="dowmload_labfilename">{{ data.filename }}.lab</a></h6>
      <iframe :src="iframe_of_labfile" width="330" >
        この部分はインラインフレームを使用
      </iframe>
      
      <br><br>
      <h6><a :href="iframe_of_logfile" :download="dowmload_logfilename">{{ data.filename }}.log</a></h6>
      <iframe :src="iframe_of_logfile" width="330" >
        この部分はインラインフレームを使用
      </iframe>

      <br><br><br><br>
    </div>
    
    <div class="col-md-6 p-3 mb-5 bg-secondary text-white">
      <h3>抽出母音音声</h3><br><br>
      <div v-for= "(vowel, key, index) in data.voweldata" :key="index">
        <h6>{{ vowel.filename }}</h6>
        <audio controls :src="'data:audio/wav;base64,' + vowel.wavedata"></audio>
        <br><br>
      </div>

    </div>
  </div>
<br>

  </div>
</template>

<script>
export default {
  name: 'show',
  data () {
    return {
      token: this.$session.get("token"),
      OAuth: this.$session.get("OAuth"),
      data: [],
      error_messages: [],
      iframe_of_textfile: "",
      iframe_of_labfile: "",
      iframe_of_logfile: "",
      dowmload_textfilename:  "",
      dowmload_labfilename: "",
      dowmload_logfilename: "",
    }
  },
  created () {
    if(this.$session.get("token")){
      this.$axios.get('/api/' + this.$route.params.id,{
        headers: {
          Authorization: `Bearer `+this.$session.get('token'),
        },
        data: {}
      })
      .then(response => {
        this.data = response.data;

        this.iframe_of_textfile = URL.createObjectURL(new Blob([this.data.textfile], { type: 'text/plain' }));
        this.iframe_of_labfile = URL.createObjectURL(new Blob([this.data.labfile], { type: 'text/plain' }));
        this.iframe_of_logfile = URL.createObjectURL(new Blob([this.data.logfile], { type: 'text/plain' }));
        this.dowmload_textfilename = this.data.filename + ".txt";
        this.dowmload_labfilename = this.data.filename + ".lab";
        this.dowmload_logfilename = this.data.filename + ".log";

      })
      .catch(e => {
        if(e.request.status == 403 || e.request.status == 408){
          this.mixin_logout();
        }else{
          this.error_messages = JSON.parse(e.request.response).messages;
        }
      });
    }
  },

  methods: {
    back: function(){
      this.$router.push({ path: "/" })
    },
    delete_of_data: function(){
      if(this.$session.get("token")){
        this.$axios.delete('/api/' + this.$route.params.id,{
          headers: {
            Authorization: `Bearer `+this.$session.get('token'),
          },
          data: {}
        })
        .then(response => {
          this.$router.push('/');
          window.location.reload();
        })
        .catch(e => {
          if(e.request.status == 403 || e.request.status == 408){
            this.mixin_logout();
          }else{
            this.error_messages = JSON.parse(e.request.response).messages;
          }
        });
      }
    }
  },
  components: {
  }
  

}
</script>