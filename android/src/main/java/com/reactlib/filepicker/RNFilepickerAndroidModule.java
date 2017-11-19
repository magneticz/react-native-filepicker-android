package com.reactlib.filepicker;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.File;
import java.util.ArrayList;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class RNFilepickerAndroidModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final int PICK_IMAGE = 1;
    private int MAX_ATTACHMENT_COUNT = 10;
    private ArrayList<String> photoPaths = new ArrayList<>();
    private ArrayList<String> docPaths = new ArrayList<>();
    private ArrayList<String> aaa= new ArrayList<>();

    private Callback pickerSuccessCallback;
    private Callback pickerCancelCallback;
  private final ReactApplicationContext reactContext;

  public RNFilepickerAndroidModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactContext.addActivityEventListener(this);
  }

  @Override
  public String getName() {
    return "RNFilepickerAndroid";
  }
  
  @ReactMethod
    public void openDocs(ReadableMap config, Callback successCallback, Callback cancelCallback) {
        Activity currentActivity = getCurrentActivity();
        int count = 20;
        if(config.getInt("count")>0) count = config.getInt("count");

        if (currentActivity == null) {
            cancelCallback.invoke("Activity doesn't exist");
            return;
        }

        pickerSuccessCallback = successCallback;
        pickerCancelCallback = cancelCallback;
        try {
            FilePickerBuilder.getInstance().setMaxCount(count)
                    .setSelectedFiles(aaa)
                    .pickFile(currentActivity);
        } catch (Exception e) {
            cancelCallback.invoke(e);
        }
    }

    @ReactMethod
    public void openGallery(ReadableMap config, Callback successCallback, Callback cancelCallback) {
        Activity currentActivity = getCurrentActivity();
        int count = 20;
        if(config.getInt("count")>0) count = config.getInt("count");
        boolean showVideo = false;
        if(config.getBoolean("showVideo")) showVideo= config.getBoolean("showVideo");

        if (currentActivity == null) {
            cancelCallback.invoke("Activity doesn't exist");
            return;
        }

        pickerSuccessCallback = successCallback;
        pickerCancelCallback = cancelCallback;
        try {
            FilePickerBuilder.getInstance().setMaxCount(count)
                    .setSelectedFiles(aaa)
                    .enableVideoPicker(true)
                    .pickPhoto(currentActivity);
        } catch (Exception e) {
            cancelCallback.invoke(e);
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.d("myTag", "This is my message");
        switch (requestCode)
        {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if(resultCode== Activity.RESULT_OK && data!=null)
                {
                    photoPaths = new ArrayList<>();
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    WritableArray photos = new WritableNativeArray();
                    for (String g: photoPaths) {
                        WritableMap item = new WritableNativeMap();
                        item.putString("mime", getMimeType(g));
                        item.putInt("size", (int) new File(g).length());
                        item.putString("path", "file://" + g);
                        photos.pushMap(item);
                    }
                    if (pickerSuccessCallback != null) {
                        if (photoPaths == null) {
                            pickerCancelCallback.invoke("No image data found can't be");
                        } else {
                            try {
                                pickerSuccessCallback.invoke(photos);
                            } catch (Exception e) {
                                pickerCancelCallback.invoke("No image data found error");
                            }
                        }
                    }

                }
                break;

            case FilePickerConst.REQUEST_CODE_DOC:
                if(resultCode== Activity.RESULT_OK && data!=null)
                {
                    docPaths = new ArrayList<>();
                    docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    WritableArray docs = new WritableNativeArray();

                    for (String g: docPaths) {
                        WritableMap item = new WritableNativeMap();
                        item.putString("mime", getMimeType(g));
                        item.putInt("size", (int) new File(g).length());
                        item.putString("path", "file://" + g);
                        docs.pushMap(item);
                    }
                    if (pickerSuccessCallback != null) {
                        if (docPaths == null) {
                            pickerCancelCallback.invoke("No image data found can't be");
                        } else {
                            try {
                                pickerSuccessCallback.invoke(docs);
                            } catch (Exception e) {
                                pickerCancelCallback.invoke("No image data found error");
                            }
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    pickerCancelCallback.invoke("ImagePicker was cancelled");
                }
                break;
        }

    }

    @Override
    public void onNewIntent(Intent intent) {

    }
    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}