<template>
  <form @submit.prevent="register">

    <div v-for= "(message, key, index) in error_messages" :key="index">
      <b-alert show variant="danger">{{ message }}</b-alert>
    </div>    

    <!-- ユーザ名入力エリア -->
    <div class="form-group">
        <label class="control-label">ユーザ名</label>
        <b-form-input id="register_username" class="form-control" v-model= "register_username" maxlength="100" placeholder="ユーザ名" required></b-form-input>
    </div>

    <div class="form-group">
    <!-- パスワード入力エリア -->
        <label class="control-label">パスワード</label>
        <b-form-input type="password" id="register_password" v-model= "register_password" maxlength="100" placeholder="パスワード" required></b-form-input>
    </div>

    <br>

    <div class="form-group float-right">
        <b-button variant="primary" type="submit">新規登録</b-button>
    </div>

  </form>
</template>

<script>
export default {
  name: 'register',
  data: function () {
    return {
      register_username: '',
      register_password: '',
      error_messages: [],
    }
  },
  methods: {
    register: function(event){
      // ユーザ名とパスワードを取得
      this.$axios.post('', { 
          name : this.register_username, 
          password : this.register_password 
      })
      .then(response => {
        this.$session.set("SuccessFlag","新規登録が完了しました")
        window.location.reload();
      })
      .catch(e => {
        this.error_messages = JSON.parse(e.request.response).messages;
      });
    }
  },
  components: {
  }

}
</script>