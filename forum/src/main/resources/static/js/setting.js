$(function () { // 页面加载完成后执行
    $("#uploadForm").submit(upload);
});

function upload() {
    // 因为传递的参数很多所以需要使用$.ajax()
    $.ajax({
        url: "http://upload-z2.qiniup.com", // 这里的url在 七牛云的文档中
        method: "post",
        processData: false,
        contentType: false,
        data: new FormData($("#uploadForm")[0]),
        success: function (data) {
            if (data && data.code == '0') {
                // 更新头像
                $.post(
                    CONTEXT_PATH + "/user/header/updateUrl",
                    {"fileName": $("input[name='key']").val()},
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.code == '0') {
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("上传失败!")
            }
        }
    });
    return false; // 返回false表示组织表单试图提交原来的数据
}