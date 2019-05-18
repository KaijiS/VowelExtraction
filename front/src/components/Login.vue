<template>
  <div>
  
  <!-- <form @submit.prevent="login">

    <div v-for= "(message, key, index) in error_messages" :key="index">
      <b-alert show variant="danger">{{ message }}</b-alert>
    </div>

    <div class="form-group">
        <label class="control-label">ユーザ名</label>
        <b-form-input id="login_username" class="form-control" v-model= "login_username" maxlength="100" placeholder="ユーザ名" required></b-form-input>
    </div>

    <div class="form-group">
        <label class="control-label">パスワード</label>
        <b-form-input type="password" id="login_password" v-model= "login_password" maxlength="100" placeholder="パスワード" required></b-form-input>
    </div>

    <div class="form-group float-right">
        <b-button variant="primary" type="submit">ログイン</b-button>
    </div>
  </form>

  <br>
  <br>
  <br> -->

  
  <a class="btn btn-block btn-social btn-github" v-on:click="gitub_oauth">
    <span><fontAwesomeIcon :icon="{ prefix: 'fab', iconName: 'github' }" :style="{ color: 'white' }"/></span>
    <span class="social-signin-title">Sign in with GitHub</span>
  </a>
  <a class="btn btn-block btn-social btn-bitbucket" v-b-modal.modal-noimplement>
    <span><fontAwesomeIcon :icon="{ prefix: 'fab', iconName: 'bitbucket' }" :style="{ color: 'white' }"/></span>
    <span class="social-signin-title">Sign in with Bitbucket</span>
  </a>
  <a class="btn btn-block btn-social btn-google" v-b-modal.modal-noimplement>
    <span><fontAwesomeIcon :icon="{ prefix: 'fab', iconName: 'google' }" :style="{ color: 'white' }"/></span>
    <span class="social-signin-title">Sign in with Google</span>
  </a>
  <a class="btn btn-block btn-social btn-yahoo" v-b-modal.modal-noimplement>
    <span><fontAwesomeIcon :icon="{ prefix: 'fab', iconName: 'yahoo' }" :style="{ color: 'white' }"/></span>
    <span class="social-signin-title">Sign in with Yahoo</span>
  </a>
  <a class="btn btn-block btn-social btn-microsoft" v-b-modal.modal-noimplement>
    <span><fontAwesomeIcon :icon="{ prefix: 'fab', iconName: 'microsoft' }" :style="{ color: 'white' }"/></span>
    <span class="social-signin-title">Sign in with Microsoft</span>
  </a>
  <a class="btn btn-block btn-social btn-twitter" v-b-modal.modal-noimplement>
    <span><fontAwesomeIcon :icon="{ prefix: 'fab', iconName: 'twitter' }" :style="{ color: 'white' }"/></span>
    <span class="social-signin-title">Sign in with Twitter</span>
  </a>
  <a class="btn btn-block btn-social btn-instagram" v-b-modal.modal-noimplement>
    <span><fontAwesomeIcon :icon="{ prefix: 'fab', iconName: 'instagram' }" :style="{ color: 'white' }"/></span>
    <span class="social-signin-title">Sign in with Instagram</span>
  </a>
  <a class="btn btn-block btn-social btn-facebook" v-b-modal.modal-noimplement>
    <span><fontAwesomeIcon :icon="{ prefix: 'fab', iconName: 'facebook' }" :style="{ color: 'white' }"/></span>
    <span class="social-signin-title">Sign in with facebook</span>
  </a>

  

  <!-- まだ実装されていないよモーダル -->
  <b-modal id="modal-noimplement" ok-only>
      まだ実装されていません<br>
  </b-modal>

  </div>
</template>

<script>
/* fortawesome系 */
import { library } from '@fortawesome/fontawesome-svg-core'
import { fab } from '@fortawesome/free-brands-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
library.add(fab)
export default {
  name: 'loginComponent',
  data: function () {
    return {
      login_username: '',
      login_password: '',
      token:'',
      error_messages: [],
    }
  },
  methods: {
    // login: function(){
    //   this.$axios.post('', { 
    //       name : this.login_username, 
    //       password : this.login_password 
    //   })
    //   .then(response => {
    //     this.$session.start();
    //     this.$session.set('token', response.data.token);
    //     window.location.reload();
    //   })
    //   .catch(e => {
    //     this.error_messages = JSON.parse(e.request.response).messages;
    //   });
    // },
    gitub_oauth: function(){
      this.$session.remove("SuccessErrorFlag")
      this.$axios.get('/user/githuboauthlogin')
      .then(response => {
        window.location.href = response.data.authUrl;
      })
      .catch(e => {
        this.error_messages = JSON.parse(e.request.response).messages;
      });
    }
  },
  components: {
    fontAwesomeIcon: FontAwesomeIcon
  }

}
</script>

<style>
@import url("./../public/css/bootstrap-social.css");

.social-signin-title {
  color: white;
}
</style>