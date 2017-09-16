package com.droi.account.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticationService extends Service {

	private Authenticator mAuthenticator;
	
    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mAuthenticator.getIBinder();
	}

}
