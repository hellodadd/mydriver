#include "process_frame.h"
#include "LaneDetector.h"
#include "CarDetector.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/opencv.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <android/log.h>
#include <unistd.h>
#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>

using namespace std;
using namespace cv;

#define LOG_TAG "DriveAssist"
#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define IPRINTF(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define EPRINTF(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

float leftS = 0.0, rightS = 0.0, topS = 0.3;

JNIEXPORT jint JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_detectLane(
  		JNIEnv* 	env,
		jobject 	thiz,
		jint 		width,
		jint 		height,
		jbyteArray 	input,
		jfloatArray output,
		jintArray 	flag)
{
	//DPRINTF("Begin lane detect!");

	jboolean isCopy = 0;
	// the pointer to input/output/flag
	jbyte * pInput = env->GetByteArrayElements(input, &isCopy);
	jfloat * pOutput = env->GetFloatArrayElements(output, &isCopy);
	jint * pFlag = env->GetIntArrayElements(flag, &isCopy);

	// the Mat according to pInput and pOutput
	Mat srcImage(height, width, CV_8UC1, (unsigned char *)pInput);

	// Lane detect
	int hasDetectedLane = 0;
	int minVote = 10;
	double minLength = 30.0;
	double maxGap = 5.0;

    //add by zhuqichao start
    //DPRINTF("缩放前：srcImage.rows = %d, srcImage.cols = %d\n", srcImage.rows, srcImage.cols);
    cv::Mat smallImg;
    cv::Size size( cvRound (srcImage.cols / LaneDetector::scale), cvRound (srcImage.rows / LaneDetector::scale) );
    resize( srcImage, smallImg, size, 0, 0, INTER_LINEAR ); //将尺寸缩小到1/scale，用线性插值
    srcImage = smallImg.clone();
    //DPRINTF("缩放后：srcImage.rows = %d, srcImage.cols = %d\n", srcImage.rows, srcImage.cols);
    //add by zhuqichao end

    LaneDetector::shift = srcImage.rows * LaneDetector::topShift;
	int min_distance_with_mid = 180;
	LaneDetector ld(srcImage, minVote, minLength, maxGap, min_distance_with_mid);
	ld.setPointsPtr(pOutput);
	ld.setFlagPtr(pFlag);
	ld.set_min_length_between_two_lines(srcImage.cols / 3);
	ld.set_max_length_between_two_lines(srcImage.cols);
	hasDetectedLane = ld.detectLane();

	// release
	env->ReleaseByteArrayElements(input, pInput, 0);
	env->ReleaseFloatArrayElements(output, pOutput, 0);
	env->ReleaseIntArrayElements(flag, pFlag, 0);

	//DPRINTF("End lane detect!");
	return hasDetectedLane;
}

JNIEXPORT jint JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_detectCar(
		JNIEnv* 	env,
		jobject 	thiz,
		jint 		width,
		jint 		height,
		jfloat 		focalLength,	//焦距(mm)
		jfloat		sensorWidth,	//sensor的宽(mm)
		jbyteArray 	input,
		jfloatArray carData,
		jintArray 	flag,
		jstring 	fileName)
{
	//DPRINTF("Begin car detect!");
	jboolean isCopy = 0;
	jbyte *pInput = env->GetByteArrayElements(input, &isCopy);
	jfloat *pCarData = env->GetFloatArrayElements(carData, &isCopy);
	jint *pFlag = env->GetIntArrayElements(flag, &isCopy);
	const char *file = env->GetStringUTFChars(fileName, &isCopy);

	string cascadeName(file);
	Mat srcImage(height, width, CV_8UC1, (unsigned char *)pInput);

    CascadeClassifier cascade; 			//创建级联分类器对象
   	//double scale = 1.2; 				//将尺寸缩小到1/scale
	if( !cascade.load( cascadeName ) ) 	//加载级联分类器，并判断是否加载成功
	{
     	cerr << "ERROR: Could not load classifier cascade" << endl;
    	return 0;
	}

	int detected_value[100];

	// 执行车辆检测
	CarDetector::leftStart = leftS;
	CarDetector::topStart = topS;
	CarDetector::width = 1 - leftS - rightS;
	CarDetector::height = 1 - topS;
	CarDetector::detect_car(srcImage, cascade, detected_value);


	float realWidthOfCar = 2;		//实际车宽(mm)
	float imageWidth = srcImage.cols;	//图片的宽(pixel)
	float imageWidthOfCar = 0.;			//车在图片中的宽(pixel)
	float distance = 0.;				//计算所得的车距(m)

	// detected_value[0]为检测到的车辆数
	// 将x和y坐标转换成比值，并传给carData数组，第5个值为一组，前4个值分别为对应的左上角x和y坐标
	// 第5个值为与此车的距离
	for (int i = 0; i != detected_value[0]; i++) {
		pCarData[5*i] = static_cast<float>(detected_value[4*i+1]) / static_cast<float>(width);
		pCarData[5*i+1] = static_cast<float>(detected_value[4*i+2]) / static_cast<float>(height);
		pCarData[5*i+2] = static_cast<float>(detected_value[4*i+3]) / static_cast<float>(width);
		pCarData[5*i+3] = static_cast<float>(detected_value[4*i+4]) / static_cast<float>(height);

		imageWidthOfCar = detected_value[4*i+3];					//车在图片中的宽
		distance = focalLength * realWidthOfCar * imageWidth
				/ (imageWidthOfCar * sensorWidth * CarDetector::scale);			//计算所得的车距
		pCarData[5*i+4] = distance;
	}
	pFlag[0] = detected_value[0];

	env->ReleaseByteArrayElements(input, pInput, 0);
	env->ReleaseFloatArrayElements(carData, pCarData, 0);
	env->ReleaseIntArrayElements(flag, pFlag, 0);

	//DPRINTF("End car detect!");
}

JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarLeft(
		JNIEnv 	*env,
		jobject	obj,
		jfloat	leftShift) {
	leftS = leftShift;
}

JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarRight(
		JNIEnv 	*env,
		jobject	obj,
		jfloat	rightShift) {
	rightS = rightShift;
}

JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarTop(
		JNIEnv 	*env,
		jobject	obj,
		jfloat	topShift) {
	topS = topShift;
}

JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setLaneTop(
		JNIEnv 	*env,
		jobject	obj,
		jfloat	topShift) {
	LaneDetector::topShift = topShift;
}


/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setCarScale
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarScale(
		JNIEnv 	*env,
		jobject	obj,
		jfloat	scale) {
	CarDetector::scale = scale;
}

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setLaneScale
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setLaneScale(
		JNIEnv 	*env,
		jobject	obj,
		jfloat	scale) {
	LaneDetector::scale = scale;
}

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setCarMinSize
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarMinSize(
		JNIEnv 	*env,
		jobject	obj,
		jint	minSize) {
	CarDetector::minSize = minSize;
}

/*
 * Class:     com_droi_adas_sdk_AdasFrameProcessor
 * Method:    setCarThreshold
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_droi_adas_sdk_AdasFrameProcessor_setCarThreshold(
		JNIEnv 	*env,
		jobject	obj,
		jint	threshold) {
	CarDetector::threshold = threshold;
}
