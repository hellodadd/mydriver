package com.droi.edriver.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class MenuRightAnimations {

	public static void playTranslateAnim(int duration, final View view, int x, int y, final boolean show, final boolean gone) {
		ObjectAnimator objAnimeX = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, x);
		ObjectAnimator objAnimeY = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, y);
		ObjectAnimator objAnimeA;
		if (show) {
			view.setAlpha(0);
			view.setVisibility(View.VISIBLE);
			objAnimeA = ObjectAnimator.ofFloat(view, View.ALPHA, 1);
		} else {
			objAnimeA = ObjectAnimator.ofFloat(view, View.ALPHA, 0);
		}
		AnimatorSet anime = new AnimatorSet();
		anime.playTogether(objAnimeX, objAnimeY, objAnimeA);
		anime.setInterpolator(new OvershootInterpolator(0.8F));
		anime.setDuration(duration);
		anime.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				if (show) {
					view.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (gone) {
					view.setVisibility(View.GONE);
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});
		anime.start();
	}

	public static void playAlphaAnime(int duration, View view) {
		ObjectAnimator objAnime = ObjectAnimator.ofFloat(view, View.ALPHA, 1);
		objAnime.setDuration(duration);
		objAnime.start();
	}
}