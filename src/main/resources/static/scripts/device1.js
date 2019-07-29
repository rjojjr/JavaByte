function checkDeviceID(){
    var isNew = Android.newDevice();
    if(isNew){
        return -1;
    }else{
        return Android.getDeviceID();
    }
}

function setDeviceID(deviceID) {
    Android.setDeviceID(deviceID);
}

function getStoredSession() {
    return Android
}

