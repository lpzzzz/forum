$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    // 将发布框关闭
    $("#publishModal").modal("hide");

    // 发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
   /* var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });*/
    //获取到页面的内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    $.post(
        CONTEXT_PATH + "/addDiscussPost",
        {"title": title, "content": content},
        function (data) {
            // 需要将data字符串转换为对象
            data = $.parseJSON(data);
            //设置提示消息
            $("#hintBody").text(data.msg);
            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 如果发布成功就刷新页面
                if (data.code == 0) {
                    //刷新页面
                    window.location.reload()
                }
            }, 888);
        }
    );
}