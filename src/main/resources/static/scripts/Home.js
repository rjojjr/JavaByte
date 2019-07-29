var context_path = "${pageContext.request.contextPath}";
var socket = new SockJS('/ws');
//var socket = new SockJS('/specific/message');
var stompClient = Stomp.over(socket);
//var connid = utils.random_string(8);
var sessionId = "";

var username = "null";

var deviceCert = "null"

var ip = "null";

function setIP(newip) {
    ip = newip;
}

function setDeviceCert(cert) {
    deviceCert = cert;

}

function setUname(Uname) {
    username = Uname;
}

var connect_callback = function () {

    // called back after the client is connected and authenticated to the STOMP server

    stompClient.subscribe('/user/queue/notify', function (msgOut) {
        var msg = msgOut.body;
        msg = msg.split("%");
        //0 = kick, 1 = device cert, 3 = display msg, 2 revalidate page
        if (msg[0] == 1) {
            writeCert(msg[1]);
        }
        if (msg[0] == 2) {
            location.href = "/?user=" + username;
        }
        if (msg[0] == 3) {
            var msg = 'Message From: ' + msg[1] + '\n' + msg[2];
            alert(msg);
        }
        if (msg[0] == 4) {
            //handle
        }
        if (msg[0] == 0) {
            location.href = "/notallowed"
        }
    });
    sendUsername();
    if (deviceCert != "null") {
        sendDeviceCert(deviceCert);
    }
};

stompClient.connect({}, function (frame) {
    var suffix = frame.headers['queue-suffix'];
    var url = stompClient.ws._transport.url;
    sessionId = /\/([^\/]+)\/websocket/.exec(socket._transport.url)[1];
    //sessionId = /\/([^\/]+)\/websocket/.exec(socket._transport.url)[1];
    url = url.replace(
        "/ws://localhost:8080/messagesocket/specific/message//ig", "");
    url = url.replace("//websocket/ig", "");
    url = url.replace(/^[0-9]+\//, "");
    console.log("Your current session is: " + sessionId);
    connect_callback()

});


function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function sendUsername() {
    stompClient.send("/app/register", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function sendDeviceCert(cert) {
    stompClient.send("/app/device/cert", {},
        JSON.stringify({'cert':cert, 'user':username, 'ip':ip}));
}

function writeCert(cert) {
    if (typeof (Storage) !== "undefined") {
        // Code for localStorage
        localStorage.setItem("cert", cert);
    }
}

function checkStatus() {
    stompClient.send("/app/httpstatus", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function getCert() {
    if (typeof (Storage) !== "undefined") {
        // Code for localStorage
        if (localStorage.length == 0) {
            setDeviceCert(0)
        } else {
            if (localStorage.getItem("cert") > "1") {
                setDeviceCert(localStorage.getItem("cert"))
            }else {

            }
        }
    } else {
        setDeviceCert(-1);
    }
}