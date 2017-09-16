#ifndef CARDETECTOR_HPP
#define CARDETECTOR_HPP

#include "opencv2/core/core.hpp"
#include "opencv2/objdetect/objdetect.hpp"

class CarDetector {
public:
	static float leftStart;
	static float topStart;
	static float width;
	static float height;

	static float scale;
	static int   minSize;
	static int	 threshold;

	static void detect_car(const cv::Mat &image, //输入的帧
			cv::CascadeClassifier &cascade, //级联分类器
			int *detected_value //将检测到的rect中的左上角顶点的x,y坐标及width和height值存储于此
			);
};

#endif
