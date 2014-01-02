package com.example.clingserviceforandroid;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements PropertyChangeListener, OnClickListener{

	private Button open_server;
	private Button close_server;
	
	private Map<String, Integer> keyValues = null;
	
	private AndroidUpnpService upnpService;
	private UDN udn = UDN.uniqueSystemIdentifier("Demo Galaxy Light");
	
	private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            LocalService<SwitchPower> switchPowerService = getSwitchPowerService();

            // Register the device when this activity binds to the service for the first time
            if (switchPowerService == null) {
                try {
                    LocalDevice binaryLightDevice = createDevice();

                    Toast.makeText(MainActivity.this, "设备完成注册", Toast.LENGTH_SHORT).show();
                    upnpService.getRegistry().addDevice(binaryLightDevice);

                    switchPowerService = getSwitchPowerService();

                } catch (Exception ex) {
                    Toast.makeText(MainActivity.this, "设备注册失败", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Obtain the state of the power switch and update the UI
            setLightbulb(switchPowerService.getManager().getImplementation().getStatus());

            switchPowerService.getManager().getImplementation().getPropertyChangeSupport()
                    .addPropertyChangeListener(MainActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initView();
        initKeyValues();
    }

	private void initView() {
		open_server = (Button) findViewById(R.id.open_server);
		close_server = (Button) findViewById(R.id.close_server);
		open_server.setOnClickListener(this);
		close_server.setOnClickListener(this);
	}
	
	private void initKeyValues(){
		keyValues = new HashMap<String, Integer>();
		keyValues.put(Constants.KEY_UP, KeyEvent.KEYCODE_DPAD_UP);
		keyValues.put(Constants.KEY_DOWN, KeyEvent.KEYCODE_DPAD_DOWN);
		keyValues.put(Constants.KEY_LEFT, KeyEvent.KEYCODE_DPAD_LEFT);
		keyValues.put(Constants.KEY_RIGHT, KeyEvent.KEYCODE_DPAD_RIGHT);
		keyValues.put(Constants.KEY_HOME, KeyEvent.KEYCODE_HOME);
		keyValues.put(Constants.KEY_RETURN, KeyEvent.KEYCODE_BACK);
		keyValues.put(Constants.KEY_MENU, KeyEvent.KEYCODE_MENU);
	}
	
	protected LocalService<SwitchPower> getSwitchPowerService() {
        if (upnpService == null)
            return null;

        LocalDevice binaryLightDevice;
        if ((binaryLightDevice = upnpService.getRegistry().getLocalDevice(udn, true)) == null)
            return null;

        return (LocalService<SwitchPower>)binaryLightDevice.findService(new UDAServiceType("SwitchPower", 1));
    }
	
	protected LocalDevice createDevice()
            throws ValidationException, LocalServiceBindingException {
		
		new DeviceIdentity(
                UDN.uniqueSystemIdentifier("Demo Galaxy Light")
        );
		
        DeviceType type =
                new UDADeviceType("BinaryLight", 1);

        DeviceDetails details =
                new DeviceDetails(
                        "Ha Ha Ha",
                        new ManufacturerDetails("ACME"),
                        new ModelDetails("Ha Ha Ha 2000", "A demo light with on/off switch.", "v1")
                );

        LocalService service = new AnnotationLocalServiceBinder().read(SwitchPower.class);

        service.setManager(new DefaultServiceManager<SwitchPower>(service, SwitchPower.class));

        return new LocalDevice(new DeviceIdentity(udn),type,details,service);
    }

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals("status")) {
            System.out.println("Turning light: " + arg0.getNewValue());
            setLightbulb((String) arg0.getNewValue());
        }
	}
	
    protected void setLightbulb(final String on) {
        runOnUiThread(new Runnable() {
            public void run() {
            	Toast.makeText(MainActivity.this, "接收到值为 : "+on, 0).show();
            }
        });
        if(keyValues.containsKey(on)){
        	KeyBoradController(keyValues.get(on));
        }
    }

    public void KeyBoradController(final int code){
    	new Thread(new Runnable() {
    		@Override
			public void run() {
    			try{
    				new Instrumentation().sendKeyDownUpSync(code);
    			}catch (Exception e) {
    				e.printStackTrace();
    			}
			}
		}).start();
    }
    
    
	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.close_server:
			getApplicationContext().unbindService(serviceConnection);
			break;
		case R.id.open_server:
			getApplicationContext().bindService(new Intent(this,BrowserUpnpService.class), serviceConnection, Context.BIND_AUTO_CREATE);
			break;
		}
	}
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		event.getKeyCode();
		return super.dispatchKeyEvent(event);
	}
}
