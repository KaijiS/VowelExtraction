<template>
    <div>
        <b-navbar toggleable="lg" type="dark" variant="dark">
            <b-navbar-brand to="/"><div class="title"> VowelExtraction</div></b-navbar-brand>

            <b-navbar-toggle target="nav-collapse"></b-navbar-toggle>

            <b-collapse id="nav-collapse" is-nav>

            <b-navbar-nav class="ml-auto">

                <div v-if= "token">
                    <b-nav-item-dropdown class="right" right>
                    <template slot="button-content">
                        <img :src="avatar_url" class="nav-user-avatar" />
                        {{ name }} さん
                    </template>
                    <b-dropdown-item v-on:click="logout">ログアウト</b-dropdown-item>
                    <b-dropdown-divider></b-dropdown-divider>
                    <div v-if= "OAuth">
                        <b-dropdown-item v-b-modal.modal-cancel-account>このアカウント連携を解除</b-dropdown-item>
                    </div>
                    <div v-else>
                        <b-dropdown-item v-b-modal.modal-delete-account>このアカウントを削除</b-dropdown-item>
                    </div>
                    
                    </b-nav-item-dropdown>
                </div>

                <div v-else>
                    <b-nav-item v-b-modal.modal-login righht>ログイン</b-nav-item>
                    <!-- <b-nav-item v-b-modal.modal-register right>新規登録</b-nav-item> -->
                </div>
            </b-navbar-nav>
            </b-collapse>
        </b-navbar>

        <!-- 新規登録のモーダル内容 -->
        <b-modal id="modal-register" title="新規登録" hide-footer>
            <registerComponent></registerComponent>
        </b-modal>

        <!-- ログインのモーダル内容 -->
        <b-modal id="modal-login" title="ログイン" hide-footer>
            <loginComponent></loginComponent>
        </b-modal>

        <!-- アカウント削除のモーダル内容 -->
        <b-modal id="modal-delete-account" title="アカウントの削除" @ok="delete_account" ok-variant="danger" ok-title=削除>
            データも削除されます<br>
            本当にアカウントを削除しますか？<br>
        </b-modal>

        <b-modal id="modal-cancel-account" title="アカウント連携の解除" @ok="delete_account" ok-variant="danger" ok-title=解除>
            データも削除されます<br>
            本当にアカウント連携を解除しますか？<br>
        </b-modal>
    </div>
    
          
</template>

<script>
import RegisterComponent from './Register.vue'
import LoginComponent from './Login.vue'
export default {
    name: 'headerComponent',
    data () {
        return {
            token: '',
            OAuth: false,
            name: "Guest",
            avatar_url: require('./../assets/guest.png'),
        }
    },
    methods: {
        logout: function () {
            // apiでログアウト処理
            this.mixin_logout();
        },
        delete_account(bvModalEvt) {
            // モーダルを閉じないようにする
            bvModalEvt.preventDefault()
            this.$axios.delete('/user',{
                headers: {
                    Authorization: `Bearer `+this.$session.get('token'),
                },
            })
            .then(response => {
                // セッション削除しtopへ戻る
                this.$session.destroy()
                this.$router.push({ path: "/"})
                window.location.reload();
            })
            .catch(e => {
                //
            });
            
        },
    },
    components: {
        registerComponent: RegisterComponent,
        loginComponent:    LoginComponent,
    },
    created () {
        // this.$session.destroy()
        if(this.$session.get("OAuth")) {
            this.OAuth = this.$session.get("OAuth");
        }
        if(this.$session.get("token")){
            this.token = this.$session.get("token");
            this.$axios.get('/user',{
                headers: {
                    Authorization: `Bearer `+this.$session.get('token'),
                },
                data: {},
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
                this.mixin_logout();
            });
        }
    }

}
</script>


<style>
div.title {
    font-size: 26px
}

img.nav-user-avatar {
    max-width: 25px;
    height: auto;
}
</style>