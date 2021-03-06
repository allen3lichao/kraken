/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_krakenapps_winapi_Process */

#ifndef _Included_org_krakenapps_winapi_Process
#define _Included_org_krakenapps_winapi_Process
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_krakenapps_winapi_Process
 * Method:    EnumProcesses
 * Signature: (Lorg/krakenapps/winapi/impl/ObjectRef;)I
 */
JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_EnumProcesses
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_krakenapps_winapi_Process
 * Method:    OpenProcess
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_OpenProcess
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     org_krakenapps_winapi_Process
 * Method:    CloseHandle
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_CloseHandle
  (JNIEnv *, jobject, jint);


JNIEXPORT jstring JNICALL Java_org_krakenapps_winapi_Process_GetModuleBaseName
  (JNIEnv *, jobject, jint, jint);

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_GetProcessTimes
(JNIEnv *env, jobject obj, jint hProcess, jobject creationTime, jobject exitTime, jobject kernelTime, jobject userTime);

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_GetProcessorCount
(JNIEnv *env, jobject obj);

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_GetProcessMemoryInfo
(JNIEnv *env, jobject obj, jint hProcess, jobject pageFaultCount, jobject peakWorkingSetSize, jobject workingSetSize, 
 jobject quotaPeakPagedPoolUsage, jobject quotaPagedPoolUsage, jobject quotaPeakNonPagedPoolUsage, jobject quotaNonPagedPoolUsage, 
 jobject pagefileUsage, jobject peakPagefileUsage, jobject privateUsage);

#ifdef __cplusplus
}
#endif
#endif
