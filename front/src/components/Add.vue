<template>

    <!-- アップロード部分&変換 -->
    <div class="add-block bg-light text-dark kakomi-box">

        <div v-if= "error_messages.length > 0">
          <div v-for= "(message, key, index) in error_messages" :key="index">
            <b-alert show variant="danger">{{ message }}</b-alert>
          </div> 
          <br>
        </div>

        <div class="container">
        <form @submit.prevent="add">
          <div class="row">
            <!-- ファイルアップロード -->
            <div class="form-group col-md-4">
                <label>wavファイルとtxtファイルを選択</label>
                <div>
                  <span class="btn btn-outline-info file">
                    {{ selectfile }}
                    <input type="file" class="uploadfile" v-on:change="onChangeFile" accept="audio/wav,text/plain" multiple>
                  </span>
                  <span>
                    <div v-for= "(uploadfile, key, index) in uploadfiles" :key="index" class="uploadfilename-fontsize">
                      {{ uploadfile }}
                    </div>
                  </span>
              </div>
            </div>

            <!-- 母音選択 -->
            <div class="form-group col-md-3">
              <label>母音を選択</label>
              <select class="form-control" v-model="vowel">
                <option disabled value="">母音を選択</option>
                <option>a</option>
                <option>i</option>
                <option>u</option>
                <option>e</option>
                <option>o</option>
              </select>
            </div>
            <div class="col-md-3 d-flex align-items-center">
              <b-button variant="primary" type="submit" class="mx-auto">母音抽出を実行</b-button>
            </div>
            <div class="col-md-2">
              <i class="fa fa-info-circle info-circle" v-b-modal.modal-noimplement></i> 
            </div>
        </div>
      </form>
      </div>

      <b-modal  id="modal-noimplement" size="lg" ok-only>
        <informationComponent></informationComponent>
      </b-modal>

  </div>
      
</template>

<script>
import InformationComponent from './Information.vue'
export default {
  name: 'add',
  data () {
    return {
      wavedata:       '',
      wave_filename:  '',
      text:           '',
      text_filename:  '',
      vowel:          '',
      error_messages: [],
      error_messages_tmp: [],
      uploadfiles: ["選択されていません"],
      selectfile: "+ ファイルを選択",
    }
  },
  methods: {
    add: function(){
      if(!this.wavedata){
        this.error_messages_tmp.push("wavとtxtの2種類のファイルを選択してください");
      }
      if(this.vowel==''){
        this.error_messages_tmp.push("母音を選択してください");
      }
      if(this.error_messages_tmp.length>0){
        this.error_messages = this.error_messages_tmp;
        this.error_messages_tmp = [];
        return; 
      }
      this.$axios.post('/api', { 
          wavedata :      this.wavedata, 
          waveFilename :  this.wave_filename,
          textdata:       this.text,
          textFilename:   this.text_filename,
          vowel:          this.vowel,
        },
        {headers: {
          Authorization: `Bearer `+this.$session.get('token'),
        }
      })
      .then(response => {
        this.error_messages = [],
        this.$session.set("SuccessFlag","処理が完了しました");
        this.$router.push('/')
        window.location.reload();
      })
      .catch(e => {
        if(e.request.status == 403 || e.request.status == 408){
          this.mixin_logout();
        }else{
          this.error_messages = JSON.parse(e.request.response).messages;
        }
      }); 

    },

    onChangeFile: function (event) {      
      const files = event.target.files;
      if (files.length == 0) {
         this.selectfile  = "+ ファイルを選択"
         this.uploadfiles = ["選択されていません"]
        return; 
      }
      this.selectfile = files.length + "個のファイルが選択済"
      if (files.length != 2) {
         this.error_messages_tmp.push("wavとtxtの2種類のファイルを選択してください");
        return; 
      }
      this.uploadfiles = [];
      let flag = 0; // wavファイルの処理を終えたら1, txtファイルの処理を終えたら2
      for (var i=0; i<files.length; i++){
        this.uploadfiles.push(files[i].name);
        if (files[i].type == "text/plain" && flag!=1) {
          this.text_filename = files[i].name;
          const tmp = this;
          const reader = new FileReader();
          reader.onload = function(e){
            tmp.text = e.target.result;
          }
          reader.readAsText(files[i]);
          flag = 1;
        } else if (files[i].type == "audio/wav" && flag!=2) {
          this.wave_filename = files[i].name;
          const tmp = this;
          const reader = new FileReader();
          reader.onload = function(e){
            tmp.wavedata = e.target.result;
          }
          reader.readAsDataURL(files[i]);
          flag = 2;
        } else {
          this.error_messages_tmp.push("wavとtxtの2種類のファイルを選択してください");
        }
      }
      
    },
  },
  components: {
    informationComponent: InformationComponent
  }
}
</script>


<style>
@import url("https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css");

.add-block{
  padding: 20px 15px;
  margin: 15px 0px;
}

.kakomi-box {
 color: #666; /* 文字色 */
 background-color: #f7f7f7; /* 背景色 */
 border: 2px solid #ccc; /* 枠線 */
}

.file {
    display: inline-block;
    overflow: hidden;
    position: relative;
    padding: 1em;
    border: 1px solid #dd0000;
    background: #dd0000;
    color:#fff;
}

.file input[type="file"] {
    opacity: 0;
    filter: progid:DXImageTransform.Microsoft.Alpha(opacity=0);
    position: absolute;
    right: 0;
    top: 0;
    margin: 0;
    font-size: 100px;
    cursor: pointer;
}

.uploadfilename-fontsize {
  font-size:13px
}

.info-circle {
  font-size: 28px;
  position:absolute;
  top: 0%;
  right: 0%;
  cursor:pointer;
}

</style>