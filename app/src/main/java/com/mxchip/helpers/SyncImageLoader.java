package com.mxchip.helpers;

import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Rocke on 2016/03/16.
 */
public class SyncImageLoader {

    private String TAG = "---SyncImageLoader---";

    private Object lock = new Object();

    private boolean mAllowLoad = true;

    private boolean firstLoad = true;

    private int mStartLoadLimit = 0;

    private int mStopLoadLimit = 0;

    final Handler handler = new Handler();

    private HashMap<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();

    public interface OnImageLoadListener {
        public void onImageLoad(BookModel model, Integer t, Drawable drawable);

        public void onError(Integer t);
    }

    public void setLoadLimit(int startLoadLimit, int stopLoadLimit) {
        if (startLoadLimit > stopLoadLimit) {
            return;
        }
        mStartLoadLimit = startLoadLimit;
        mStopLoadLimit = stopLoadLimit;
    }

    public void restore() {
        mAllowLoad = true;
        firstLoad = true;
    }

    public void lock() {
        mAllowLoad = false;
        firstLoad = false;
    }

    public void unlock() {
        mAllowLoad = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void loadImage(Integer t, BookModel model, String imageUrl, OnImageLoadListener listener) {
        final OnImageLoadListener mListener = listener;
        final String mImageUrl = imageUrl;
        final Integer mt = t;

        final BookModel mymodel = model;

        new Thread(new Runnable() {

            @Override
            public void run() {
                if (!mAllowLoad) {
                    Log.d(TAG, "prepare to load");
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (mAllowLoad && firstLoad) {
                    loadImage(mymodel, mImageUrl, mt, mListener);
                }

                if (mAllowLoad && mt <= mStopLoadLimit && mt >= mStartLoadLimit) {
                    loadImage(mymodel, mImageUrl, mt, mListener);
                }
            }

        }).start();
    }

    private void loadImage(final BookModel model, final String mImageUrl, final Integer mt, final OnImageLoadListener mListener) {

        if (imageCache.containsKey(mImageUrl)) {
            SoftReference<Drawable> softReference = imageCache.get(mImageUrl);
            final Drawable d = softReference.get();
            if (d != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mAllowLoad) {
                            mListener.onImageLoad(model, mt, d);
                        }
                    }
                });
                return;
            }
        }
        try {
            final Drawable d = loadImageFromUrl(mImageUrl);
            if (d != null) {
                imageCache.put(mImageUrl, new SoftReference<Drawable>(d));
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mAllowLoad) {
                        mListener.onImageLoad(model, mt, d);
                    }
                }
            });
        } catch (IOException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onError(mt);
                }
            });
            e.printStackTrace();
        }
    }

    public static Drawable loadImageFromUrl(String url) throws IOException {
        Log.d("---loadImageFromUrl---", url);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File f = new File(Environment.getExternalStorageDirectory() + "/iBakImgs/" + MD5.getMD5(url));
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                Drawable d = Drawable.createFromStream(fis, "src");
                return d;
            }
            URL m = new URL(url);
            InputStream i = (InputStream) m.getContent();
            DataInputStream in = new DataInputStream(i);
            FileOutputStream out = new FileOutputStream(f);
            byte[] buffer = new byte[1024];
            int byteread = 0;
            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
            in.close();
            out.close();
            Drawable d = Drawable.createFromStream(i, "src");
            return loadImageFromUrl(url);
        } else {
            URL m = new URL(url);
            InputStream i = (InputStream) m.getContent();
            Drawable d = Drawable.createFromStream(i, "src");
            return d;
        }
    }
}
