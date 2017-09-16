#include "CarDetector.h"

#include <iostream>
#include <vector>

#include "opencv2/core/core.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/objdetect/objdetect.hpp"

using namespace std;
using namespace cv;

float CarDetector::leftStart = 0.0;
float CarDetector::topStart = 1.0/3;
float CarDetector::width = 1.0;
float CarDetector::height = 2.0/3;

float CarDetector::scale = 1.2;
int   CarDetector::minSize = 10;
int   CarDetector::threshold = 10;

void CarDetector::detect_car(const cv::Mat &inputImg, //输入的帧
			cv::CascadeClassifier &cascade, //级联分类器
			int *detected_value //将检测到的rect中的左上角顶点的x,y坐标及width和height值存储于此
			)
{
    	int i = 0;
    	vector<Rect> cars; //检测到的车

    	Rect roi(inputImg.cols*leftStart,
		inputImg.rows*topStart,
		inputImg.cols*width,
		inputImg.rows*height);
    	Mat roiImg = inputImg(roi);
    	Mat gray, smallImg( cvRound (roiImg.cols/scale), cvRound(roiImg.rows/scale), CV_8UC1 ); //缩小图片，加快检测

    	resize( roiImg, smallImg, smallImg.size(), 0, 0, INTER_LINEAR ); //将尺寸缩小到1/scale，用线性插值
    	equalizeHist( smallImg, smallImg ); //直方图均衡，提高图片亮度
	
	/**
	 * 检测人脸
	 * smallImg表示要检测的输入图像
	 * cars表示检测到的车目标序列
	 * 1.1表示每次图像尺寸减小的比例为1.1
	 * 2表示每一个目标至少要被检测到3次才算是真的目标（因为周围的像素和不同的窗口大小都可以检测到车）
	 * CV_HAAR_SCALE_IMAGE表示不是缩放分类器来检测，而缩放图像
	 * Size(10, 10) 和 Size(80, 80)为目标的最小最大尺寸
	 */
    	cascade.detectMultiScale( 
		smallImg, 
		cars,
        1.1,
        threshold,
		0
        //|CV_HAAR_FIND_BIGGEST_OBJECT
		//|CV_HAAR_DO_ROUGH_SEARCH
        |CV_HAAR_SCALE_IMAGE,
		Size(minSize, minSize));

	
	/* 将检测到的车区域信息存储至detected_value中 */
	int &cars_num = *detected_value++; //第一个值用于存储检测到的车辆数
	int count = 0;
    	for( vector<Rect>::const_iterator r = cars.begin(); r != cars.end(); r++, i++ )
    	{
    	*detected_value++ = r->x * scale + inputImg.cols*leftStart;
    	*detected_value++ = r->y * scale + inputImg.rows*topStart;
		*detected_value++ = r->width * scale;
		*detected_value++ = r->height * scale;
		count++;
    	}
	cars_num = count;

}
