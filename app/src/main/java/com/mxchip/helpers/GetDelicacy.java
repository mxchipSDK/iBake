package com.mxchip.helpers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mico.micosdk.MiCOUser;
import com.mxchip.activities.ibake.R;
import com.mxchip.callbacks.UserCallBack;
import com.mxchip.manage.ConstHelper;
import com.mxchip.manage.ConstPara;
import com.mxchip.manage.SharePreHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rocke on 2016/03/25.
 */
public class GetDelicacy {
    private String TAG = "---GetDelicacy---";

    private SyncImageLoader syncImageLoader;
    private Activity mactivity;

    private TextView txt1;

    private TextView like_no_txt1;

    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private ImageView iv4;
    private ImageView iv5;
    private LinearLayout sweet_time_lycontain;

    public GetDelicacy(Activity activity) {
        syncImageLoader = new SyncImageLoader();
        this.mactivity = activity;
        initView();
    }

    private void initView() {

        sweet_time_lycontain = (LinearLayout) mactivity.findViewById(R.id.sweet_time_lycontain);
    }

    public void refreshDelicy() {
        getSweetTime();
    }

    SyncImageLoader.OnImageLoadListener imageLoadListener = new SyncImageLoader.OnImageLoadListener() {

        @Override
        public void onImageLoad(BookModel model, Integer i, Drawable drawable) {

            final Bitmap bitmap = ConstHelper.drawable2Bitmap(drawable);
            switch (i) {
                case 1:
                    iv1.setImageBitmap(bitmap);
                    break;
                case 2:
                    iv2.setImageBitmap(bitmap);
                    break;
                case 3:
                    iv3.setImageBitmap(bitmap);
                    break;
                case 4:
                    iv4.setImageBitmap(bitmap);
                    break;
                case 5:
                    iv5.setImageBitmap(bitmap);
                    break;
            }
//                setimg.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                });
        }

        @Override
        public void onError(Integer t) {
//            BookModel model = (BookModel) getItem(t);
//            View view = mListView.findViewWithTag(model);
//            if (view != null) {
//                ImageView iv = (ImageView) view.findViewById(R.id.dev_item_imgid);
//                iv.setBackgroundResource(R.drawable.mydevice_icon_device_online);
//            }
        }
    };

    public void getSweetTime() {
        sweet_time_lycontain.removeAllViews();
        final String imgurl1 = "http://sh.sinaimg.cn/2011/1115/U5839P18DT20111115095540.jpg";

        MiCOUser micoUser = new MiCOUser();
        int type = 2;
        String productid = "6486b2d1-0ee9-4647-baa3-78b9cbc778f7";
        SharePreHelper shareph = new SharePreHelper(mactivity);
        String token = shareph.getData(ConstPara.SHARE_TOKEN);
        micoUser.getCookBookList(type, productid, new UserCallBack() {
            @Override
            public void onSuccess(String message) {

                Log.d(TAG + "onSuccess", message);
                try {
                    JSONArray itemsArr = new JSONArray(ConstHelper.getFogData(message));

                    JSONObject items = null;
                    String recName = "";
                    String likeno = "";

                    for (int i = 0; i < itemsArr.length() && i < 5; i++) {
                        items = itemsArr.getJSONObject(i);
                        recName = items.getString("name");
                        likeno = items.getString("favorite_count");

                        //有一个就新建一个view
                        View viewOne1 = mactivity.getLayoutInflater().inflate(R.layout.homepage_sweet_time_item, null);
                        sweet_time_lycontain.addView(viewOne1, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

                        //view的名字1
                        txt1 = (TextView) viewOne1.findViewById(R.id.delicacy_1_text);

                        //view的图片
                        switch (i) {
                            case 0:
                                iv1 = (ImageView) viewOne1.findViewById(R.id.delicacy_1_pic);
                                break;
                            case 1:
                                iv2 = (ImageView) viewOne1.findViewById(R.id.delicacy_1_pic);
                                break;
                            case 2:
                                iv3 = (ImageView) viewOne1.findViewById(R.id.delicacy_1_pic);
                                break;
                            case 3:
                                iv4 = (ImageView) viewOne1.findViewById(R.id.delicacy_1_pic);
                                break;
                            case 4:
                                iv5 = (ImageView) viewOne1.findViewById(R.id.delicacy_1_pic);
                                break;
                        }
                        syncImageLoader.loadImage(i + 1, null, items.getString("mainimageurl"), imageLoadListener);

                        //view的点赞个数
                        like_no_txt1 = (TextView) viewOne1.findViewById(R.id.delicacy_1_like_no);

                        //分别给名字、点赞个数、图片赋值
                        txt1.setText(i + "wode");
                        like_no_txt1.setText(i + "10");
                        syncImageLoader.loadImage(i + 1, null, imgurl1, imageLoadListener);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int code, String message) {
                Log.d(TAG + "onFailure", code + " " + message);
            }
        }, token);
    }
}
