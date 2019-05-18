<template>

  <div class="text-center">
    <img :src="avatar_url" class="user-avatar"><br>
    <br>
    <div>{{name}}</div>
    <br><br>
  </div>
      
</template>

<script>
export default {
  name: 'user_info',
  data () {
    return {
      token: '',
      OAuth: false,
      name: "Guest",
      avatar_url: require('./../assets/guest.png'),
    }
  },
  created () {
    if(this.$session.get("OAuth")) this.OAuth = this.$session.get("OAuth");
    if(this.$session.get("token")){
      this.token = this.$session.get("token");
      this.$axios.get('/user',{
        headers: {
          Authorization: `Bearer `+this.$session.get('token'),
        },
      })
      .then(response => {
        this.name = response.data.name;
        if(this.$session.get("OAuth")){
          this.avatar_url = response.data.avatarUrl;
        }else{
          this.avatar_url = require('./../assets/user.png');
        }
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
}
</script>


<style>
img.user-avatar{
    max-width: 250px;
    height: auto;
}
</style>