<template>
  <div>
    <div class="container-fluid">
        <div class="row">
          <div class="col-md-3" style="word-wrap:break-word;">
              <userInfoComponent></userInfoComponent>
          </div>
          <div class="col-md-8">
              <router-view></router-view>
          </div>
        </div>
    </div>
  </div>
</template>

<script>
import UserInfoComponent from './UserInfo.vue'
export default {
  name: 'baseComponent',
  data () {
    return {
      
    }
  },
  methods: {
      
  },
  components: {
    userInfoComponent: UserInfoComponent
  }
}
</script>

<style>

</style>
