<script>
export default {
    methods: {
        mixin_logout() {
            // apiでログアウト処理
            this.$axios.put('/user/logout',{
                data: {}
            },{
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
        }
    },
}
</script>