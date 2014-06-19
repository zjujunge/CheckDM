LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_SRC_FILES := $(call all-java-files-under, $(src))
LOCAL_PACKAGE_NAME := CheckDM
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PROGUARD_ENABLED := disabled
include $(BUILD_PACKAGE)
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
####################################
include $(CLEAR_VARS)
include $(BUILD_MULTI_PREBUILT)

