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
