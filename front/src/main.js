import Vue from 'vue'
import App from './App.vue'
import BaseComponent from './components/Base.vue'
import HomeComponent from './components/Home.vue'
import ShowComponent from './components/Show.vue'
import SessionComponent from './components/Session.vue'
import ErrorComponent from './components/Error.vue'
import mixin_logout from './components/mixins/mixin_logout.vue'

import VueRouter from 'vue-router'
import VueSession from 'vue-session'
import BootstrapVue from 'bootstrap-vue' // added

import 'bootstrap/dist/css/bootstrap.css' // added
import 'bootstrap-vue/dist/bootstrap-vue.css' // added

Vue.config.productionTip = false

import axiosBase from 'axios'

const axios = axiosBase.create({
  baseURL: 'http://localhost:9000', 
  headers: {
    'Content-Type': 'application/json',
  },
});

Vue.prototype.$axios = axios;

Vue.use(VueSession);
Vue.use(BootstrapVue);
Vue.use(VueRouter);
Vue.mixin(mixin_logout);

/* eslint-disable */

// api叩くときのリクエスト内容の表示
axios.interceptors.request.use(request => {
  console.log('Starting Request: ', request)
  return request
})

// api叩いたあとのレスポンス内容の表示
axios.interceptors.response.use(response => {
  console.log('Response: ', response)
  return response
})

var router = new VueRouter({
  routes : [
    // ここにルートを記述
    { path: '/', component: BaseComponent,
      children:[
        { path: '', component: HomeComponent },
        { path: 'show/:id', component: ShowComponent },
      ]
    },
    { path: '/Session', component: SessionComponent },
    { path: '/*', component: ErrorComponent },
  ],
  mode: 'history'
})

/* eslint-enable */

new Vue({
  render: h => h(App),
  router
}).$mount('#app')
