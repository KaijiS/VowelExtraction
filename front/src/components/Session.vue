<template>
  <div>
  </div>
</template>

<script>
export default {
  name: 'sessionComponent',
  data: function() {
    return {
    }
  },
  created () {
    this.$axios.get('/user/getToken?onetimetoken='+this.$route.query.onetimetoken)
    .then(response => {
      this.$session.start();
      this.$session.set('token', response.data.token);
      this.$session.set('OAuth', true);
      this.$router.push('/')
      window.location.reload();
    })
    .catch(e => {
      alert(JSON.parse(e.request.response).messages);
    });
  }
  
}
</script>


<style>

</style>