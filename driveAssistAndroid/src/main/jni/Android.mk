LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include /mnt/one/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := process
LOCAL_SRC_FILES := LaneDetector.cpp CarDetector.cpp process_frame.cpp 

LOCAL_LDLIBS +=   -ldl -std=c++11	#for C++11
LOCAL_LDLIBS    += -llog 			#for logging
LOCAL_LDLIBS    += -landroid  		#for native asset manager

LOCAL_CPPFLAGS += -std=c++11

include $(BUILD_SHARED_LIBRARY)
