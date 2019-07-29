function reboot() {
    var result = confirm("Do you really want to reboot the server?");
    if (result == true) {
        alert("Rebooting Server...");
        location.href = "/maintenance/reboot"
    }
    else {
        alert("Reboot cancelled");
    }
}

function shutdown() {
    var result = confirm("Do you really want to shutdown the server?");
    if (result == true) {
        alert("Shutting down the Server...");
        location.href = "/maintenance/shutdown"
    }
    else {
        alert("Shutdown cancelled");
    }
}

var x = document.getElementById("reboot");
x.onclick = reboot();

var y = document.getElementById("shutdown");
y.onclick = shutdown();