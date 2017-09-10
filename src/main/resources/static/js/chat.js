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
    $('.error-tip').text('');
    var reg = /^[0-9a-zA-Z]{6,10}$/;

    // 验证用户昵称
    if (!reg.test(username)) {
        $('.error-tip').text('（请输入6~10位数字和英文字母组合）');
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
        bootbox.alert({
            title: '提示',
            message: '您的浏览器不支持WebSocket，请切换到chrome获取最佳体验！'
        });
        websocket = new SockJS('http://' + path + '/sockjs-chatHandler')
    }

    // 配置WebSocket连接生命周期
    websocket.onopen = function () {
        console.log('WebSocket open!');
    };

    websocket.onmessage = function (event) {
        handleMessage(event);
    };

    websocket.onerror = function () {
        console.log('WebSocket error!');
        bootbox.alert({
            title: '提示',
            message: 'WebSocket连接异常，请刷新页面！',
            callback: function () {
                window.location.reload();
            }
        });
    };

    websocket.onclose = function () {
        console.log('WebSocket close!');
        bootbox.alert({
            title: '提示',
            message: 'WebSocket连接断开，请刷新页面！',
            callback: function () {
                window.location.reload();
            }
        });
    };

    window.onbeforeunload = function () {
        websocket.close();
    };
}

// 本地httpSessionId
var localSessionId;

var $chatInput = $('.chat-input');
var $chatArea = $('.chat-area');

/**
 * 处理收到的服务端响应，根据消息类型调用响应处理方法
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

    switch (type) {
        case 'chat':
            handleChatMessage(httpSessionId, username, payload);
            break;
        case 'online':
            console.log('online: ' + username);
            handleSystemMessage(username, type);
            break;
        case 'offline':
            console.log('offline: ' + username);
            handleSystemMessage(username, type);
            break;
        case 'error':
            console.log('error: ' + username);
            handleSystemMessage(username, type);
            break;
        case 'time':
            console.log('time: ' + payload);
            handleSystemMessage(null, type, payload);
            break;
        case 'list':
            handleUserList(payload);
            break;
        case 'authenticate':
            console.log('authenticate: ' + httpSessionId);
            localSessionId = httpSessionId;
            break;
        default:
            bootbox.alert({
                title: '提示',
                message: 'Unexpected message type.'
            });
            handleSystemMessage(null, type);
    }
}

/**
 * 处理聊天文本信息
 * 将本地用户消息与其它用户消息区分
 */
function handleChatMessage(httpSessionId, username, payload) {
    if (localSessionId === httpSessionId) {
        $chatArea.append(
            '<div class="chat-content chat-content-mine">' +
            '<span>' + username + '</span>' +
            '<div class="chat-text chat-text-mine">' +
            payload +
            '</div>' +
            '<div class="chat-ico chat-ico-mine">' +
            '<img src="image/chat-ico.png">' +
            '</div>' +
            '</div>'
        );
    } else {
        $chatArea.append(
            '<div class="chat-content chat-content-theirs">' +
            '<div class="chat-ico chat-ico-theirs">' +
            '<img src="image/chat-ico.png">' +
            '</div>' +
            '<span>' + username + '</span>' +
            '<div class="chat-text chat-text-theirs">' +
            payload +
            '</div>' +
            '</div>'
        );
    }
    // 将聊天内容区域滚动条置底
    endScroll();
}

/**
 * 维护在线列表
 * @param payload
 */
function handleUserList(payload) {
    var list = '';
    $.each(payload, function (index, el) {
        var username = el.username;
        var host = el.host;
        list +=
            '<div class="chat-bar-per">' +
            '<span class="glyphicon glyphicon-user"></span>' +
            username +
            '<div>' +
            host +
            '</div>' +
            '</div>';
    });
    $('.chat-bar-list').html(list);
}

/**
 * 处理系统消息
 * @param username
 * @param type
 * @param payload
 */
function handleSystemMessage(username, type, payload) {
    var message = '';
    switch (type) {
        case 'online':
            message = '上线了';
            break;
        case  'offline':
            message = '下线了';
            break;
        case 'error':
            message = '掉线了';
            break;
        case 'time':
            message = payload;
            break;
        default:
            message = '系统炸了';
    }

    if (null !== username) {
        $chatArea.append(
            '<div class="chat-content chat-content-system">' + '用户' +
            '<span>' + username + '</span>' + message +
            '</div>'
        );
    } else {
        if ('time' === type) {
            $chatArea.append(
                '<div class="chat-content chat-content-system">' + payload + '</div>'
            );
        } else {
            $chatArea.append(
                '<div class="chat-content chat-content-system">' + message + '</div>'
            );
        }
    }

    // 将聊天内容区域滚动条置底
    endScroll();
}

/**
 * 查看在线列表
 */
$('#view-online').click(function () {
    $('.chat-bar').fadeToggle(500);
});

/**
 * 发送消息
 */
$('#send').click(function () {
    var chatText = $chatInput.text().trim();
    if (chatText.length === 0) {
        bootbox.alert({
            title: '提示',
            message: '发送内容不可为空！'
        });
    } else if (chatText.length > 120) {
        bootbox.alert({
            title: '提示',
            message: '发送内容过长！'
        });
    } else {
        $chatInput.empty();
        websocket.send(chatText);
        textNum(0);
    }
});

/**
 * 显示字数统计
 * @param length
 */
function textNum(length) {
    $('.chat-size').text(length + '/120');
}

/**
 * 字数统计
 */
textNum(0);
$chatInput.on('focus paste input', function () {
    var length = $chatInput.text().length;
    textNum(length);

    var input = $chatInput[0];
    input.scrollTop = input.scrollHeight;
});

/**
 * 聊天显示区域自动到底
 */
function endScroll() {
    var area = $chatArea[0];
    area.scrollTop = area.scrollHeight;
}