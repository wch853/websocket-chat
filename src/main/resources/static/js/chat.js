bootbox.setLocale('zh_CN');

/**
 * 验证是否存在用户信息
 * 若不存在，要求输入用户信息
 * 若存在，建立WebSocket连接
 */
function verifyUser() {
    $.ajax({
        url: 'verifyUser',
        success: function (res) {
            if (null === res || res.length === 0) {
                $('#username-modal').modal({
                    backdrop: 'static',
                    keyboard: false
                });
            } else {
                getConnect();
            }
        },
        error: function () {
            bootbox.alert({
                title: '提示',
                message: '连接失败，请重新连接！',
                callback: function () {
                    window.location.reload();
                }
            });
        }
    });
}

verifyUser();

/**
 * 添加用户信息
 */
$('#save-username').click(function () {
    var username = $('#add-username').val().trim();
    $('#error-tip').text('');
    var reg = /^[0-9a-zA-Z]{6,16}$/;

    // 验证用户昵称
    if (!reg.test(username)) {
        $('#error-tip').text('（聊天昵称输入有误）');
    } else {
        $.ajax({
           url: 'addUser',
           data: {
               username: username
           },
           success: function () {
               $('#username-modal').modal('hide');
               getConnect();
           }
        });
    }
});

var websocket;

/**
 * 建立WebSocket连接
 */
function getConnect() {
    var path = window.location.hostname + ":7090/" + window.location.pathname.split("/")[1];
    if (window.WebSocket) {
        console.log('Support WebSocket.');
        websocket = new WebSocket('ws://' + path + '/chatHandler');
    } else {
        console.log('Not Support WebSocket! It\'s recommended to use chrome!');
        websocket = new SockJS('http://' + path + '/sockjs-chatHandler')
    }
    websocket.onopen = function () {
        console.log('WebSocket open!');
    };

    websocket.onmessage = function (event) {
        handleMessage(event);
    };

    websocket.onerror = function () {
        alert('WebSocket error, reload the page!');
    };

    websocket.onclose = function () {
        console.log('WebSocket close!')
    };

    window.onbeforeunload = function () {
        websocket.close();
    };
}

// 本地httpSessionId
var localSessionId;

/**
 * 处理收到的消息
 */
function handleMessage(event) {
    var response = JSON.parse(event.data);

    // 获取消息类型
    var type = response.type;
    // 获取httpSessionId
    /** @namespace response.httpSessionId */
    var httpSessionId = response.httpSessionId;
    // 获取host
    var host = response.host;
    // 获取username
    var username = response.username;
    // 获取payload
    /** @namespace response.payload */
    var payload = response.payload;

    if ('chat' === type) {
        console.log('from session: ' + httpSessionId);
        var $chatArea = $('.chat-area');
        if (localSessionId === httpSessionId) {
            $chatArea.append('<div class="chat-text chat-text-mine">' + payload + '</div><br>');
        } else {
            $chatArea.append('<div class="chat-text chat-text-theirs">' + payload + '</div><br>');
        }
    } else if ('online' === type) {
        console.log('online: ' + username)
    } else if ('offline' === type) {
        console.log('offline: ' + username)
    } else if ('authenticate' === type) {
        console.log('authenticate: ' + username);
        localSessionId = httpSessionId;
    } else if ('error' === type) {
        console.log('error: ' + username);
    } else {
        bootbox.alert('Unexpected message type.')
    }
}

$('#view-online').click(function () {
    $('.chat-bar').fadeToggle(500);
});

$('#send').click(function () {
    var chatText = $('.chat-input').text();
    websocket.send(chatText);
});
