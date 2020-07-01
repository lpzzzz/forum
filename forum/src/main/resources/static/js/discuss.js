// 在按钮加载完成之后调用 方法
$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

function like(obj, entityType, entityId, entityUserId, discussPostId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId, "discussPostId": discussPostId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {// 点赞成功
                $(obj).children("i").text(data.likeCount);
                $(obj).children("b").text(data.likeStatus == 1 ? '已赞' : '赞');
            } else {
                alert(data.msg);
            }
        }
    );
}

// 置顶方法 
function setTop() {
    $.post(
        CONTEXT_PATH + "/discussTop",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == "0") {
                // 置顶之后我们的指定按钮设置为不可用状态
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg)
            }
        }
    );
}

// 加精方法
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discussWonderful",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == "0") {
                // 置顶之后我们的指定按钮设置为不可用状态
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg)
            }
        }
    );
}

/**
 * 删除方法
 */
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discussDelete",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == "0") {
                // 删除之后自动跳转到首页
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg)
            }
        }
    );
}

