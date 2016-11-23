#include <jni.h>
#include <string>

#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <linux/input.h>
#include <linux/uinput.h>
#include <android/log.h>

#include <stdio.h>
#include <errno.h>
#include <time.h>
#include <string.h>


#define TAG "EventInjector::JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)


#define die(str, args...) do { \
        perror(str); \
        exit(EXIT_FAILURE); \
} while(0)


extern "C"
jstring
Java_toinsson_uiinput_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
jstring
Java_toinsson_uiinput_ZeroMQSub_stringFromJNI(
        JNIEnv *env,
        jobject /* this */)
{
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
jint
Java_toinsson_uiinput_ZeroMQSub_printInt(
        JNIEnv *env,
        jobject /* this */,
        jint A,
        jint B)
{
    return A+B;
}


int                    fd;
struct uinput_user_dev uidev;
struct input_event     ev;
int                    dx, dy;
int                    i;

extern "C"
jstring
Java_toinsson_uiinput_ZeroMQSub_initTouchInterface(
        JNIEnv *env,
        jobject /* this */) {

//    __android_log_print(TAG, "MyTag", "The value is %d", fd);
    LOGD("init the touch interface");

    system("su 0 setenforce 0");

    LOGD("fd = %d", fd);

    fd = open("/dev/input/event0", O_WRONLY | O_NONBLOCK);

    LOGD("fd = %d", fd);

    memset(&uidev, 0, sizeof(uidev));                  //creat an virtul input device node in /dev/input/***
    snprintf(uidev.name, UINPUT_MAX_NAME_SIZE, "uinput-sample");
    uidev.id.bustype = BUS_USB;
    uidev.id.vendor  = 0x1;
    uidev.id.product = 0x1;
    uidev.id.version = 1;

    std::string hello = "Init OK";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
jstring
Java_toinsson_uiinput_ZeroMQSub_touchUp(
        JNIEnv *env,
        jobject /* this */) {

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;  //mouse left key
    ev.code = ABS_MT_TRACKING_ID;
    ev.value = 0xffffffff;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_SYN; // inform input system to process this input event
    ev.code = SYN_REPORT;
    ev.value = 0;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    std::string hello = "touch up ok";
    return env->NewStringUTF(hello.c_str());
}

int trackingId = 100;

extern "C"
jstring
Java_toinsson_uiinput_ZeroMQSub_touchDown(
        JNIEnv *env,
        jobject /* this */,
        jint X,
        jint Y)
{
    trackingId += 1;

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;  //mouse left key
    ev.code = ABS_MT_TRACKING_ID;
    ev.value = trackingId;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;  //mouse left key
    ev.code = ABS_MT_PRESSURE;
    ev.value = 20;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;         //send x coordinates
    ev.code = ABS_MT_POSITION_X;
    ev.value = X;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;  //send y coordinates
    ev.code = ABS_MT_POSITION_Y;
    ev.value = Y;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_SYN; // inform input system to process this input event
    ev.code = SYN_REPORT;
    ev.value = 0;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");


    std::string hello = "touch down ok";
    return env->NewStringUTF(hello.c_str());

}


extern "C"
jstring
Java_toinsson_uiinput_ZeroMQSub_touchMove(
        JNIEnv *env,
        jobject /* this */,
        jint X,
        jint Y)
{
//    memset(&ev, 0, sizeof(struct input_event));
//    ev.type = EV_ABS;  //mouse left key
//    ev.code = ABS_MT_TRACKING_ID;
//    ev.value = 60;
//    if(write(fd, &ev, sizeof(struct input_event)) < 0)
//        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;  //mouse left key
    ev.code = ABS_MT_PRESSURE;
    ev.value = 20;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;         //send x coordinates
    ev.code = ABS_MT_POSITION_X;
    ev.value = X;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;  //send y coordinates
    ev.code = ABS_MT_POSITION_Y;
    ev.value = Y;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_SYN; // inform input system to process this input event
    ev.code = SYN_REPORT;
    ev.value = 0;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");


    std::string hello = "touch up ok";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
jstring
Java_toinsson_uiinput_ZeroMQSub_writeEvent(
        JNIEnv *env,
        jobject /* this */) {

    switch(rand() % 4) {
        case 0:
            dx = -10;
            dy = -10;
            break;
        case 1:
            dx = 10;
            dy = 10;
            break;
        case 2:
            dx = -10;
            dy = 10;
            break;
        case 3:
            dx = 10;
            dy = -10;
            break;
    }


    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;  //mouse left key
    ev.code = ABS_MT_PRESSURE;
    ev.value = 20;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;         //send x coordinates
    ev.code = ABS_MT_POSITION_X;
    ev.value = 200 + dx;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_ABS;  //send y coordinates
    ev.code = ABS_MT_POSITION_X;
    ev.value = 200 + dy;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_SYN; // inform input system to process this input event
    ev.code = SYN_REPORT;
    ev.value = 0;
    if(write(fd, &ev, sizeof(struct input_event)) < 0)
        die("error: write");

    usleep(15000);

}



extern "C"
jstring
Java_toinsson_uiinput_MainActivity_testFunction(
        JNIEnv *env,
        jobject /* this */) {

//    int                    fd;
//    struct uinput_user_dev uidev;
//    struct input_event     ev;
//    int                    dx, dy;
//    int                    i;
//
//    fd = open("/dev/input/event0", O_WRONLY | O_NONBLOCK);
//    if(fd < 0) die("error: open");

//config uinput working mode,  mouse or touchscreen?  relative coordinates or absolute coordinate?
//    if(ioctl(fd, UI_SET_EVBIT, EV_KEY) < 0)         //support key button
//        die("error: ioctl");
//    if(ioctl(fd, UI_SET_KEYBIT, BTN_LEFT) < 0)  //support mouse left key
//        die("error: ioctl");
//
//    if(ioctl(fd, UI_SET_EVBIT, EV_REL) < 0)       //uinput use relative coordinates
//        die("error: ioctl");
//    if(ioctl(fd, UI_SET_RELBIT, REL_X) < 0)         //uinput use x coordinates
//        die("error: ioctl");
//    if(ioctl(fd, UI_SET_RELBIT, REL_Y) < 0)         //uinput use y coordinates
//        die("error: ioctl");
//
//    memset(&uidev, 0, sizeof(uidev));                  //creat an virtul input device node in /dev/input/***
//    snprintf(uidev.name, UINPUT_MAX_NAME_SIZE, "uinput-sample");
//    uidev.id.bustype = BUS_USB;
//    uidev.id.vendor  = 0x1;
//    uidev.id.product = 0x1;
//    uidev.id.version = 1;
//
//    if(write(fd, &uidev, sizeof(uidev)) < 0)
//        die("error: write");

//    if(ioctl(fd, UI_DEV_CREATE) < 0)
//        die("error: ioctl");

    sleep(2);
    srand(time(NULL));

    while(1) {
//simulate( x,y) coordinates for mouse
        switch(rand() % 4) {
            case 0:
                dx = -10;
                dy = -1;
                break;
            case 1:
                dx = 10;
                dy = 1;
                break;
            case 2:
                dx = -1;
                dy = 10;
                break;
            case 3:
                dx = 1;
                dy = -10;
                break;
        }

        for(i = 0; i < 20; i++) {
//send input event to kernel input system

            memset(&ev, 0, sizeof(struct input_event));
            ev.type = EV_ABS;  //mouse left key
            ev.code = ABS_MT_PRESSURE;
            ev.value = 20;
            if(write(fd, &ev, sizeof(struct input_event)) < 0)
                die("error: write");

            memset(&ev, 0, sizeof(struct input_event));
            ev.type = EV_ABS;         //send x coordinates
            ev.code = ABS_MT_POSITION_X;
            ev.value = 200 + dx;
            if(write(fd, &ev, sizeof(struct input_event)) < 0)
                die("error: write");

            memset(&ev, 0, sizeof(struct input_event));
            ev.type = EV_ABS;  //send y coordinates
            ev.code = ABS_MT_POSITION_X;
            ev.value = 200 + dy;
            if(write(fd, &ev, sizeof(struct input_event)) < 0)
                die("error: write");

            memset(&ev, 0, sizeof(struct input_event));
            ev.type = EV_SYN; // inform input system to process this input event
            ev.code = SYN_REPORT;
            ev.value = 0;
            if(write(fd, &ev, sizeof(struct input_event)) < 0)
                die("error: write");

            usleep(15000);
        }


        sleep(5);
    }


    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

//
//
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
////    sleep(2);
////
////    if(ioctl(fd, UI_DEV_DESTROY) < 0)
////        die("error: ioctl");
////    close(fd);
////    return 0;
//}