remember to enable the file /dev/input/eventX for writing with 

adb shell
su
chmod 777 /dev/input/eventX

adb shell getevent -l /dev/input/event0
