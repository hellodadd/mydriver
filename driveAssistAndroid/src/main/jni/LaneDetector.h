#ifndef LANEDETECTOR_HPP
#define LANEDETECTOR_HPP

#define PI 3.1415926

#include <vector>

#include "opencv2/core/core.hpp"

class LaneDetector{

private:

	struct Point_Line_Ori{
		cv::Point the_point;
		cv::Vec4i the_line;
		double the_ori;
	};

	cv::Mat inputImg;
	std::vector<cv::Vec4i> detectedLines;
	std::vector<cv::Vec4i> refineLines;
	std::vector<Point_Line_Ori> leftPointLines;
	std::vector<Point_Line_Ori> rightPointLines;
	int minVote;
	double minLength;
	double maxGap;
	int min_distance_with_mid;

	float *pointsPtr;
	int *flagPtr;

public:

    static float scale; //add by zhuqichao
    static float topShift; //add by zhuqichao
    static float shift; //add by zhuqichao

	static cv::Vec4i prev_left_line;
	static cv::Vec4i prev_right_line;
	static int left_save_frame;
	static int right_save_frame;
	static int max_save_frame;

	static int min_length_between_two_lines;
	static int max_length_between_two_lines;

	LaneDetector(cv::Mat &originImg) :
		inputImg(originImg), minVote(50), minLength(0.), maxGap(0.), min_distance_with_mid(0){}
	LaneDetector(cv::Mat &originImg, const int &setMinVote, const double &setMinLength, const double &setMaxGap, const int &set_mix_distance_with_mid) :
		inputImg(originImg), minVote(setMinVote), minLength(setMinLength), maxGap(setMaxGap), min_distance_with_mid(set_mix_distance_with_mid) {}

	int detectLane();
	void export_line_data(cv::Mat &image);
	void classify(); //divide the lines into right and left
	void bottom_point_of_line(cv::Point &point, const int &x1, const int &y1, const int &x2, const int &y2);
	cv::Point intersect_point(const cv::Vec4i&, const cv::Vec4i&);
	int refine_lines();

	void setPointsPtr(float*);
	void setFlagPtr(int*);
	void set_min_length_between_two_lines(const int&);
	void set_max_length_between_two_lines(const int&);
};

#endif
