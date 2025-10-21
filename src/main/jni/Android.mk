# Infomaniak PDFium - Android
# Copyright (C) 2025 Infomaniak Network SA
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
LOCAL_PATH := $(call my-dir)

#Prebuilt libraries
include $(CLEAR_VARS)
LOCAL_MODULE := aospPdfium

ARCH_PATH = $(TARGET_ARCH_ABI)

LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/$(ARCH_PATH)/libmodpdfium.so

include $(PREBUILT_SHARED_LIBRARY)

#libmodft2
include $(CLEAR_VARS)
LOCAL_MODULE := libmodft2

LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/$(ARCH_PATH)/libmodft2.so

include $(PREBUILT_SHARED_LIBRARY)

#libmodpng
include $(CLEAR_VARS)
LOCAL_MODULE := libmodpng

LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/$(ARCH_PATH)/libmodpng.so

include $(PREBUILT_SHARED_LIBRARY)

#Main JNI library
include $(CLEAR_VARS)
LOCAL_MODULE := jniPdfium

LOCAL_CFLAGS += -DHAVE_PTHREADS
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES += aospPdfium
LOCAL_LDLIBS += -llog -landroid -ljnigraphics

LOCAL_SRC_FILES :=  $(LOCAL_PATH)/src/mainJNILib.cpp

include $(BUILD_SHARED_LIBRARY)
