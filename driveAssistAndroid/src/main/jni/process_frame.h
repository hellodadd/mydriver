/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_hwm_app_tyd_adassecond_ADASSecond */

#ifndef _Included_org_hwm_app_tyd_adassecond_ADASSecond
#define _Included_org_hwm_app_tyd_adassecond_ADASSecond
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_hwm_app_tyd_adassecond_ADASSecond
 * Method:    detectLane
 * Signature: (II[B[F[I)Z
 */
JNIEXPORT jint JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_detectLane (
		JNIEnv* 	env,
		jobject 	thiz,
		jint 		width,
		jint 		height,
		jbyteArray 	input,
		jfloatArray output,
		jintArray 	flag);

/*
 * Class:     org_hwm_app_tyd_adassecond_ADASSecond
 * Method:    detectCar
 * Signature: (II[B[F[ILjava/lang/String;)Z
 */
JNIEXPORT jint JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_detectCar (
		JNIEnv* 	env,
		jobject 	thiz,
		jint 		width,
		jint 		height,
		jfloat 		focalLength,
		jfloat		sensorWidth,
		jbyteArray 	input,
		jfloatArray carData,
		jintArray 	flag,
		jstring 	fileName);

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setCarLeft
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarLeft
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setCarRight
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarRight
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setCarTop
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarTop
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setLaneTop
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setLaneTop
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setCarScale
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarScale
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setLaneScale
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setLaneScale
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setCarMinSize
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarMinSize
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setCarThreshold
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarThreshold
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif