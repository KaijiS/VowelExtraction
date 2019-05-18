<template>
  <div id="app">
    <headerComponent></headerComponent>

    <template v-if= "message">
      <br>
      <div class="row">
        <div class="col-sm-1">
        </div>
        <div class="col-sm-10">
          <b-alert show variant="success">{{message}}</b-alert>
        </div>
      </div>
    </template>

    <template v-if= "error_messages">
      <br>
      <div class="row">
        <div class="col-md-1">
        </div>
        <div class="col-md-10">
          <div v-for= "(error_message, key, index) in error_messages" :key="index">
            <b-alert show variant="danger">{{ error_message }}</b-alert>
          </div>
        </div>
      </div>
    </template>

    
    <router-view></router-view>
  </div>
</template>

<script>
import HeaderComponent from './components/Header.vue'
export default {
  name: 'app',
  data () {
    return {
      message: "",
      error_messages: [],
    }
  },
  created () {
      if(this.$session.get("SuccessFlag")) {
        this.message = this.$session.get("SuccessFlag");
        this.$session.remove("SuccessFlag")
      }
      if(this.$session.get("SessionErrorFlag")){
        this.error_messages = this.$session.get("SessionErrorFlag");
      }
  },
  methods: {
      
  },
  components: {
    headerComponent: HeaderComponent,
  }
}
</script>

<style>

</style>
