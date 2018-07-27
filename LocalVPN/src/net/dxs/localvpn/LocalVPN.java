package net.dxs.localvpn;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LocalVPN extends Activity {
	private static final int VPN_REQUEST_CODE = 0x0F;

	private boolean waitingForVPNStart;

	private BroadcastReceiver vpnStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (LocalVPNService.BROADCAST_VPN_STATE.equals(intent.getAction())) {
				if (intent.getBooleanExtra("running", false))
					waitingForVPNStart = false;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_vpn);
		final Button vpnButton = (Button) findViewById(R.id.vpn);
		vpnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startVPN();
			}
		});
		waitingForVPNStart = false;
		LocalBroadcastManager.getInstance(this).registerReceiver(
				vpnStateReceiver,
				new IntentFilter(LocalVPNService.BROADCAST_VPN_STATE));
		init();
	}

	private void init() {
		Button baidu = (Button) findViewById(R.id.baidu);
		Button cpsino = (Button) findViewById(R.id.cpsino);
		baidu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "http://119.75.217.109";
				new AsyncHttpClient().get(url, new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {
						Toast.makeText(LocalVPN.this,
								"baidu-onSuccess:" + responseString,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						Toast.makeText(
								LocalVPN.this,
								"baidu-onFailure:" + responseString
										+ "-statusCode:" + statusCode
										+ "-throwable:"
										+ throwable.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
		cpsino.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "http://www.cpsino.com/";
				new AsyncHttpClient().get(url, new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {
						Toast.makeText(LocalVPN.this,
								"cpsino-onSuccess:" + responseString,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						Toast.makeText(
								LocalVPN.this,
								"cpsino-onFailure:" + responseString
										+ "-statusCode:" + statusCode
										+ "-throwable:"
										+ throwable.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	private void startVPN() {
		Intent vpnIntent = VpnService.prepare(this);
		if (vpnIntent != null)
			startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
		else
			onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
			waitingForVPNStart = true;
			startService(new Intent(this, LocalVPNService.class));
			enableButton(false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		enableButton(!waitingForVPNStart && !LocalVPNService.isRunning());
	}

	private void enableButton(boolean enable) {
		final Button vpnButton = (Button) findViewById(R.id.vpn);
		if (enable) {
			vpnButton.setEnabled(true);
			vpnButton.setText(R.string.start_vpn);
		} else {
			vpnButton.setEnabled(false);
			vpnButton.setText(R.string.stop_vpn);
		}
	}
}
