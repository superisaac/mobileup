/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/

package mobileup.android;

import android.content.Context;
import mobileup.network.IFactory;
import mobileup.network.Protocol;

public class AssetFactory implements IFactory {
    public Context context;
    public AssetFactory(Context context) {
	this.context = context;
    }
    public Protocol buildProtocol() {
	return new AssetProtocol();
    }
}
