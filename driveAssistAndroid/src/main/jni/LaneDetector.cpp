#include <vector>
#include <map>
#include <iostream>

#include "LaneDetector.h"
#include "opencv2/core/core.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <android/log.h>

#define LOG_TAG "zhuqichao"
#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

using namespace std;
using namespace cv;

Vec4i LaneDetector::prev_left_line; //the detected line of last left frame
Vec4i LaneDetector::prev_right_line; //the detected line of last right frame
int LaneDetector::left_save_frame = 0; //the number of latest left frames
int LaneDetector::right_save_frame = 0; //the number of latest right frames
int LaneDetector::max_save_frame = 40; //the number of latest frames

int LaneDetector::min_length_between_two_lines = 0;
int LaneDetector::max_length_between_two_lines = 10000;

float LaneDetector::topShift = 0.7; //add by zhuqichao
float LaneDetector::shift; //add by zhuqichao
float LaneDetector::scale = 1; //add by zhuqichao

void LaneDetector::setPointsPtr(float *outputPtr) {
	pointsPtr = outputPtr;
}

void LaneDetector::setFlagPtr(int *ptr) {
	flagPtr = ptr;
}

void LaneDetector::set_min_length_between_two_lines(const int& value) {
	min_length_between_two_lines = value;
}

void LaneDetector::set_max_length_between_two_lines(const int& value) {
	max_length_between_two_lines = value;
}

/**
 * export the detected line's data
 */
void LaneDetector::export_line_data(cv::Mat &image) {
	//cout << "----------start export line data method----------" << endl;

	std::vector<cv::Vec4i>::const_iterator it= refineLines.begin();

	if (left_save_frame > max_save_frame || right_save_frame > max_save_frame) {
		for (int i = 0; i != 8; i++) {
			*pointsPtr++ = 0.;
		}
	} else {
        if(left_save_frame > 25) {
            *flagPtr = 0;
        }
		while (it != refineLines.end()) {

			*pointsPtr++ = (float)(*it)[0] / (float)inputImg.cols;
			*pointsPtr++ = (float)((*it)[1] + shift) / (float)inputImg.rows;
			*pointsPtr++ = (float)(*it)[2] / (float)inputImg.cols;
			*pointsPtr++ = (float)((*it)[3] + shift) / (float)inputImg.rows;

			++it;
		}
	}
}


/**
 * remove lines that angle is unreasonable
 */
void LaneDetector::classify(){
	//cout << "----------start classify method----------" << endl;

	vector<Vec4i>::iterator it= detectedLines.begin();
	Point_Line_Ori tmp;
	Point point;

	while (it!=detectedLines.end()) {

		const int x1 = (*it)[0];
		const int y1 = (*it)[1];
		const int x2 = (*it)[2];
		const int y2 = (*it)[3];

		double ori;
		if (y1 == y2) {
			ori = 0.;
		} else {
			//ori = atan2(static_cast<double>(y2-y1), static_cast<double>(x2-x1));
			ori = atan(static_cast<double>(y2-y1)/static_cast<double>(x2-x1));
		}

		double min_angle = PI/15; //min_angle
		double max_angle = PI/2; //max_angle

		// expand the line to the bottom of screen, to get the bottom point
		bottom_point_of_line(point, x1, y1, x2, y2);

		// (1) correct orientation (2) the bottom point is on the right
		bool isRightLine = (ori>=0 && ori>=min_angle && ori<=max_angle &&
					point.x>= inputImg.cols/2);
		// (1) correct orientation (2) the bottom point is on the left
		bool isLeftLine = (ori<0 && ori>=-max_angle && ori <=-min_angle &&
					point.x < inputImg.cols/2);

		if ((isRightLine || isLeftLine)) {
			tmp.the_point = point;
			tmp.the_line = *it;
			tmp.the_ori = ori;
			if (point.x - inputImg.cols / 2 > 0) {
				rightPointLines.push_back(tmp);
			} else {
				leftPointLines.push_back(tmp);
			}
		}
		++it;
	}
}

/**
 * expand the line to the bottom to get the point that the line intersect with bottom
 */
void LaneDetector::bottom_point_of_line(cv::Point &point, const int &x1, const int &y1, const int &x2, const int &y2){
	double leftY;
	double rightY;
	double bottomX;
	if (x1 == x2) {
		leftY = -1;
		rightY = -1;
	} else {
		leftY = -x1 * (y2 - y1) / static_cast<double>(x2 - x1) + y1; //intersection with left axis y
		rightY = (inputImg.cols - x1) * (y2 - y1) / static_cast<double>(x2 - x1) + y1; //intersection with left axis y
	}
	bottomX = (inputImg.rows - shift - y1) * (x2 - x1) / static_cast<double>(y2 - y1) + x1; //intersection with bottom axis x
	if (bottomX > 0 && bottomX < inputImg.cols) {
		point.x = bottomX;
		point.y = inputImg.rows - shift;
	} else if (leftY > 0 && leftY < inputImg.rows - shift) {
		point.x = 0;
		point.y = leftY;
	} else if (rightY > 0 && rightY < inputImg.rows - shift) {
		point.x = inputImg.cols;
		point.y = rightY;
	}
}

/**
 * find the intersect point of two lines
 */
Point LaneDetector::intersect_point(const Vec4i &l1, const Vec4i &l2)
{
	const double x1 = l1[0];
	const double y1 = l1[1];
	const double x2 = l1[2];
	const double y2 = l1[3];
	const double x3 = l2[0];
	const double y3 = l2[1];
	const double x4 = l2[2];
	const double y4 = l2[3];

	double x;
	double y;
	Point point;

	if (x1 == x2 && x3 != x4) {
		x = x1;
		y = (y4 - y3) / (x4 - x3) * (x - x3) + y3;
	} else if (x1 != x2 && x3 == x4) {
		x = x3;
		y = (y2 - y1) / (x2 - x1) * (x - x1) + y1;
	} else if (x1 == x2 && x3 == x4) { //两条平行线
		x = 0;
		y = 0;
	} else {
		x = ((y3 - y1) + (y2 - y1) / (x2 - x1) * x1 - (y4 - y3) / (x4 - x3) * x3) /
			((y2 - y1) / (x2 - x1) - (y4 - y3) / (x4 - x3));
		y = (y2 - y1) / (x2 - x1) * (x - x1) + y1;
	}

	point.x = static_cast<int>(x);
	point.y = static_cast<int>(y);
	return point;
}

/**
 * refine the line set
 */
int LaneDetector::refine_lines(){
	//cout << "----------start refine_lines method----------" << endl;

	bool right_has = false;
	bool left_has = false;

	Vec4i best_left_line;
	Vec4i best_right_line;

	double best_left_ori = 0.0;
	double best_right_ori = 0.0;

	int best_left_bottom_x = 0;
	int best_left_bottom_y = 0;
	int best_right_bottom_x = inputImg.cols;
	int best_right_bottom_y = 0;

	// the most likely right line
	vector<Point_Line_Ori>::const_iterator right_it = rightPointLines.begin();
	while (right_it != rightPointLines.end()) {
		//pick the closest line with midpoint
		if ((*right_it).the_point.x <= best_right_bottom_x) {
			best_right_line = (*right_it).the_line;
			best_right_ori = (*right_it).the_ori;
			best_right_bottom_x = (*right_it).the_point.x; // the x of bottom point of this line
			best_right_bottom_y = (*right_it).the_point.y; // the y of bottom point of this line
			right_has = true;
		}
		right_it++;
	}

	// the most likely left line, it's same as the above
	vector<Point_Line_Ori>::const_iterator left_it = leftPointLines.begin();
	while (left_it != leftPointLines.end()) {
		//pick the closest line with midpoint
		if ((*left_it).the_point.x >= best_left_bottom_x) {
			best_left_line = (*left_it).the_line;
			best_left_ori = (*left_it).the_ori;
			best_left_bottom_x = (*left_it).the_point.x; // the x of bottom point of this line
			best_left_bottom_y = (*left_it).the_point.y; // the y of bottom point of this line
			left_has = true;
		}
		left_it++;
	}

	// if the two line's intersecting point is close to bottom, the lines will be removed
	// and the intersect angle of these two lines has a range
	if (right_has && left_has) {
		Point point = intersect_point(best_left_line, best_right_line);
		if (point.y + shift > inputImg.rows * 3  / 4) {
			right_has = false;
			left_has = false;
		}
		if (PI - best_right_ori -abs(best_left_ori) < 1.5 ||
			PI - best_right_ori -abs(best_left_ori) > 1.96) {
			right_has = false;
			left_has = false;
		}
	}

	// compare with preview detected lines to identify whether it's correct
	bool prev_is_empty = prev_right_line[0] == 0 &&
				prev_right_line[1] == 0 &&
				prev_right_line[2] == 0 &&
				prev_right_line[3] == 0;
	if (right_save_frame < 5 && !prev_is_empty) {
		int width_threshold = 60;
		double angle_threshold = PI / 20;

		if (left_has && right_has) {
			// width change
			int width_change = (best_right_bottom_x - best_left_bottom_x) -
				(prev_right_line[2] - prev_left_line[0]);
			// left width change
			double left_width_change = best_left_bottom_x - prev_left_line[0];
			// right width change
			double right_width_change = best_right_bottom_x - prev_right_line[2];
			// right line angle change
			double prev_right_angle = atan2(static_cast<double>(prev_right_line[3] - prev_right_line[1]), static_cast<double>(prev_right_line[2] - prev_right_line[0]));
			double right_angle_change = abs(prev_right_angle - best_right_ori);
			// left line angle change
			double prev_left_angle = atan2(static_cast<double>(prev_left_line[3] - prev_left_line[1]), static_cast<double>(prev_left_line[2] - prev_left_line[0]));
			double left_angle_change = abs(prev_left_angle - best_left_ori);

			// it's impossible that the line has a big change int short time
			if (abs(width_change) > width_threshold ||
				abs(right_angle_change) > angle_threshold ||
				abs(left_angle_change) > angle_threshold) {
				right_has = false;
				left_has = false;
			}

			// first situation: right deviation
			// second situation: left deviation
			if ((left_width_change > width_threshold &&
				right_width_change > width_threshold &&
				right_angle_change < -angle_threshold &&
				left_angle_change < -angle_threshold )
			|| (left_width_change < -width_threshold &&
				right_width_change < -width_threshold &&
				right_angle_change > angle_threshold &&
				left_angle_change > angle_threshold))
			{
				right_has = true;
				left_has = true;
			}
		}
	}

	//expand the line to the bottom
	if (right_has && left_has) {

		best_right_line[2] = best_right_bottom_x;
		best_right_line[3] = best_right_bottom_y;
		prev_right_line = best_right_line;
		right_save_frame = 0;

		best_left_line[0] = best_left_bottom_x;
		best_left_line[1] = best_left_bottom_y;
		prev_left_line = best_left_line;
		left_save_frame = 0;

		refineLines.push_back(best_right_line);
		refineLines.push_back(best_left_line);

	} else if (right_save_frame < max_save_frame && left_save_frame < max_save_frame) {

		best_right_line = prev_right_line;
		best_right_bottom_x = best_right_line[2];
		best_right_bottom_y = best_right_line[3];
		right_save_frame++;

		best_left_line = prev_left_line;
		best_left_bottom_x = best_left_line[0];
		best_left_bottom_y = best_left_line[1];
		left_save_frame++;

		refineLines.push_back(best_right_line);
		refineLines.push_back(best_left_line);
	}

	// Judge that whether the car has deviate the correct lane
    int relativeRef = abs((inputImg.cols / 2 - best_left_bottom_x) - (best_right_bottom_x - inputImg.cols / 2));
    int absoluteRef = 0.5 * (best_right_bottom_x - best_left_bottom_x);
	if ((relativeRef > absoluteRef)) {
		*flagPtr = 1;
	} else {
		*flagPtr = 0;
	}

	// make left_line and right_line have the same height
	vector<Vec4i>::iterator first= refineLines.begin(); // the right line
	if (first != refineLines.end()) { // at least exist one line
		vector<Vec4i>::iterator second = first + 1; // the left line
		if (second != refineLines.end()){ // the two lines all exist
			// if the bottom point of two lines is too close, it's unreasonable
			if ((*first)[2] - (*second)[0] > min_length_between_two_lines) {
				if ((*first)[1] > (*second)[3]) {
					(*first)[0] = ((*second)[3] - (*first)[1]) * ((*first)[2] - (*first)[0]) / static_cast<double>((*first)[3] - (*first)[1]) + (*first)[0];
					(*first)[1] = (*second)[3];
				} else {
					(*second)[2] = ((*first)[1] - (*second)[1]) * ((*second)[2] - (*second)[0]) / static_cast<double>((*second)[3] - (*second)[1]) + (*second)[0];
					(*second)[3] = (*first)[1];
				}
			} else {
				refineLines.clear(); //clear the situation that two lines are too close
			}
		} else {
			refineLines.clear(); //clear the situation that only has one line
		}
	}

	if ((right_has && left_has) ||
			(right_save_frame < max_save_frame && left_save_frame < max_save_frame)) {
		return 1;
	} else {
		return 0;
	}
}


int LaneDetector::detectLane(){

	// set the ROI for the image
	cv::Rect roi(0, shift, inputImg.cols, inputImg.rows - shift);
	cv::Mat roiImg = inputImg(roi);

	// Canny algorithm
	cv::Mat cannyImg;
	Canny(roiImg,cannyImg, 50, 150);

	// Hough tranform
	detectedLines.clear();
	cv::HoughLinesP(cannyImg, detectedLines, 1, PI/180, minVote, minLength, maxGap);
	classify();
	int hasDetectedLane = refine_lines();
	export_line_data(inputImg);
	return hasDetectedLane;
}
