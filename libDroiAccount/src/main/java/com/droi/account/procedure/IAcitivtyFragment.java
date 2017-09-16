package com.droi.account.procedure;

import android.content.Intent;

public interface IAcitivtyFragment {

	public void onFragmentResult(int requestCode, int resultCode, Intent data);
	public void setSelectedFragment(BackHandledFragment selectedFragment);
}
