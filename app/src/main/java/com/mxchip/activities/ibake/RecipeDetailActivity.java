package com.mxchip.activities.ibake;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.trade.TradeConstants;
import com.alibaba.sdk.android.trade.TradeService;
import com.alibaba.sdk.android.trade.page.ItemDetailPage;
import com.mico.micosdk.MiCOCookBook;
import com.mico.micosdk.MiCODevice;
import com.mxchip.callbacks.ControlDeviceCallBack;
import com.mxchip.callbacks.MiCOCallBack;
import com.mxchip.helper.ScheduleTaskParam;
import com.mxchip.helpers.BookModel;
import com.mxchip.helpers.SyncImageLoader;
import com.mxchip.manage.ConstHelper;
import com.mxchip.manage.ConstPara;
import com.mxchip.manage.SetTitleBar;
import com.mxchip.manage.SharePreHelper;
import com.taobao.tae.sdk.model.TaokeParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rocke on 2016/03/29.
 */
public class RecipeDetailActivity extends AppCompatActivity {
    private static final String TAG = "---RecDeAc---";
    private SetTitleBar stb;

    private ListView recipe_dd_material_lv;
    private LinearLayout recipe_detial_showtaobao;
    private LinearLayout recipe_detial_ilikeit;
    private LinearLayout recipe_detial_delayCmd;

    private ImageView recipe_detial_img;

    private String itemid = ConstPara._ITEMID;
    private MiCODevice micoDev;
    private SharePreHelper shareph;

    private MiCOCookBook micocookbook;
    int recipeid;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_detail);

        String recipename = (String) getIntent().getSerializableExtra(ConstPara.INTENT_RECIPENAME);
        stb = new SetTitleBar(RecipeDetailActivity.this);
        stb.setTitleName(recipename);
        stb.setLeftButton("back", "finish");
        stb.setRightButton("edit", "edit");

        micocookbook = new MiCOCookBook();
        recipeid = Integer.parseInt((String) getIntent().getSerializableExtra(ConstPara.INTENT_RECIPEID));
        shareph = new SharePreHelper(RecipeDetailActivity.this);
        token = shareph.getData(ConstPara.SHARE_TOKEN);

        initView();

        initOnClick();
        getThisBookInfo();

        micoDev = new MiCODevice(RecipeDetailActivity.this);
        shareph = new SharePreHelper(RecipeDetailActivity.this);
    }

    private  void initView(){
        recipe_dd_material_lv = (ListView) findViewById(R.id.recipe_dd_material_lv);
        recipe_detial_showtaobao = (LinearLayout) findViewById(R.id.recipe_detial_showtaobao);
        recipe_detial_ilikeit = (LinearLayout) findViewById(R.id.recipe_detial_ilikeit);
        recipe_detial_delayCmd = (LinearLayout) findViewById(R.id.recipe_detial_delayCmd);

        recipe_detial_img = (ImageView)findViewById(R.id.recipe_detial_img);
    }

    //    获取一共需要哪些食材
    private void initMaterial(JSONArray ingredients) {
        SimpleAdapter adapter = new SimpleAdapter(this, getData(ingredients), R.layout.recipe_dd_material_item,
                new String[]{"name", "kg"},
                new int[]{R.id.recipe_dd_buttertv, R.id.recipe_dd_butterkgtv});
        recipe_dd_material_lv.setAdapter(adapter);
    }

    //通过itemid打开淘宝的宝贝详情界面
    private static void showItemDetailPage(Activity context, String isvcode, String itemid) {
        TradeService tradeService = AlibabaSDK.getService(TradeService.class);
        Map<String, String> exParams = new HashMap<String, String>();

        if(!ConstHelper.checkPara(isvcode))
            isvcode = ConstPara._ISVCODE;

        exParams.put(TradeConstants.ISV_CODE, isvcode);
        exParams.put(TradeConstants.ITEM_DETAIL_VIEW_TYPE, TradeConstants.TAOBAO_NATIVE_VIEW);
        ItemDetailPage itemDetailPage = new ItemDetailPage(itemid, exParams);
        TaokeParams taokeParams = new TaokeParams();
        taokeParams.pid = ConstPara._MMPID;
        tradeService.show(itemDetailPage, taokeParams, context, null, new com.alibaba.sdk.android.trade.callback.TradeProcessCallback() {
            @Override
            public void onPaySuccess(com.alibaba.sdk.android.trade.model.TradeResult tradeResult) {
                Log.d(TAG, tradeResult.toString());
            }

            @Override
            public void onFailure(int i, String s) {
                Log.d(TAG, s);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, requestCode+"");
        Log.d(TAG, resultCode+"");

        Log.d(TAG, "---onActivityResult---");
    }

    private void initOnClick() {
        ConstHelper.initAlibabaSDK(RecipeDetailActivity.this);

        //打开淘宝的界面
        recipe_detial_showtaobao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showItemDetailPage(RecipeDetailActivity.this, null, itemid);
            }
        });

        //点个赞
        recipe_detial_ilikeit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer integer = (Integer) recipe_detial_img.getTag();
                integer = integer == null ? 0 : integer;
                if (integer == R.drawable.recipe_icon_like) {
                    //TODO 取消点赞 delCookBookLikeNo
                    recipe_dd_likeno_txt.setText(like_count + "");
                    recipe_detial_img.setImageResource(R.drawable.recipe_icon_favourate_3);
                    recipe_detial_img.setTag(R.drawable.recipe_icon_favourate_3);

//                    micocookbook.delCookBookLikeNo(recipeid, new MiCOCallBack() {
//                        @Override
//                        public void onSuccess(String message) {
//                            Log.d(TAG, message);
//                            if (ConstHelper.getFogCode(message).equals(ConstPara._SUCCESSCODE)) {
//                                recipe_dd_likeno_txt.setText(like_count + "");
//                                recipe_detial_img.setImageResource(R.drawable.recipe_icon_favourate_3);
//                                recipe_detial_img.setTag(R.drawable.recipe_icon_favourate_3);
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(int code, String message) {
//                            Log.d(TAG, code + "msg = " + message);
//                            ConstHelper.setToast(RecipeDetailActivity.this, ConstHelper.getFogMessage(message));
//                        }
//                    }, token);
                } else {
                    //recipe_icon_favourate_3
//                    micocookbook.addCookBookFavoriteNo(recipeid, new MiCOCallBack() {
                    micocookbook.addCookBookLikeNo(recipeid, new MiCOCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            Log.d(TAG, message);

                            if (ConstHelper.getFogCode(message).equals(ConstPara._SUCCESSCODE)) {
                                recipe_dd_likeno_txt.setText((like_count + 1) + "");
                                recipe_detial_img.setImageResource(R.drawable.recipe_icon_like);
                                recipe_detial_img.setTag(R.drawable.recipe_icon_like);
                            }
                        }

                        @Override
                        public void onFailure(int code, String message) {
                            Log.d(TAG, code + "msg = " + message);
                            ConstHelper.setToast(RecipeDetailActivity.this, ConstHelper.getFogMessage(message));
                        }
                    }, token);
                }

            }
        });

        recipe_detial_delayCmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDelayCommand();
            }
        });
    }

    private void sendDelayCommand(){

        ScheduleTaskParam stp = new ScheduleTaskParam();
        stp.device_id = "d95366fe-06c0-11e6-a739-00163e0204c0";
        stp.commands = "{\"KG_Bottom\":\"1\",\"KG_Start\":\"1\",\"KG_Top\":\"1\",\"Temp_Bottom\":\"240\",\"Temp_Top\":\"240\",\"WorkMode\":\"1\",\"WorkTime\":\"58\",\"appid\":\"d8cdf9c6-de8c-11e5-a739-00163e0204c0\",\"deviceid\":\"d95366fe-06c0-11e6-a739-00163e0204c0\",\"userid\":\"b8b917e2-deaa-11e5-a739-00163e0204c0\",\"atrrSet\":[\"KG_Bottom\",\"KG_Start\",\"KG_Top\",\"Temp_Bottom\",\"Temp_Top\",\"WorkMode\",\"WorkTime\"]}";
        stp.enable = true;

        stp.month = "*";
//        stp.day_of_month = "*";
        stp.day_of_week = "*";
        stp.hour = "*";
        stp.minute = "*";

//        stp.second = 10;

//        micoDev.creatDelayTask(stp, new ControlDeviceCallBack() {
//        micoDev.createScheduleTask(stp, new ControlDeviceCallBack() {
//            @Override
//            public void onSuccess(String message) {
//                Log.d(TAG + "onSuccess", message);
//            }
//
//            @Override
//            public void onFailure(int code, String message) {
//                Log.d(TAG + "onFailure", code + " " + message);
//            }
//        }, token);

        micoDev.getTaskList("d95366fe-06c0-11e6-a739-00163e0204c0", 1, new ControlDeviceCallBack() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG + "onSuccess", message);
            }

            @Override
            public void onFailure(int code, String message) {
                Log.d(TAG + "onFailure", code + " " + message);
            }
        }, token);
    }

    /**
     * 更新材料列表
     * @return
     */
    private List<Map<String, Object>> getData(JSONArray ingredients) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        ViewGroup.LayoutParams params = recipe_dd_material_lv.getLayoutParams();
        int i = 0;
        try {
            for (; i < ingredients.length(); i++) {
                JSONObject temp = (JSONObject) ingredients.get(i);
                map = new HashMap<String, Object>();
                map.put("name", temp.getString("item_name"));
                map.put("kg", temp.getString("quantity"));
                list.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //动态设置listview的高度
        params.height = ConstHelper.dip2px(RecipeDetailActivity.this, 20 * i);
        recipe_dd_material_lv.setLayoutParams(params);

        return list;
    }

    private void addLinearLayout(JSONArray steps) {
        LinearLayout recipe_dd_more_lys = (LinearLayout) findViewById(R.id.recipe_dd_more_lys);
        recipe_dd_more_lys.removeAllViews();

        try {
            for (int i = 0; i < steps.length(); i++) {
//            for (int i = steps.length(); i > 0; i--) {
                JSONObject temp = (JSONObject) steps.get(i);

                String num_step = temp.getString("num_step");
                String step_description = temp.getString("step_description");

                //更新步骤
                View viewOne1 = getLayoutInflater().inflate(R.layout.recipe_bake_steps, null);
                recipe_dd_more_lys.addView(viewOne1, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
              // addNowMaterial(viewOne1);

                //更新步骤tag
                TextView recipe_bake_step_no = (TextView)viewOne1.findViewById(R.id.recipe_bake_step_no);
                recipe_bake_step_no.setText(num_step + "/" + steps.length());
                //更新步骤描述
                TextView recipe_bake_step_des = (TextView)viewOne1.findViewById(R.id.recipe_bake_step_des);
                recipe_bake_step_des.setText(step_description);

                // 如果有图片
              // String imgurl = temp.getString("mainimageurl");
              // if(ConstHelper.checkPara(imgurl)){
              //     SyncImageLoader syncImageLoader = new SyncImageLoader();
              //     syncImageLoader.loadImage(HOME_PAGE, null, imgurl, imageLoadListener);
              // }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //TODO 每个步骤的所需食材
//    private void addNowMaterial(View view) {
//        ListView nowlv = (ListView) view.findViewById(R.id.recipe_bake_step_material_lv);
//
//        SimpleAdapter adapter = new SimpleAdapter(this, geOnetData(nowlv), R.layout.recipe_dd_material_item,
              // new String[]{"name", "kg"},
              // new int[]{R.id.recipe_dd_buttertv, R.id.recipe_dd_butterkgtv});
//        nowlv.setAdapter(adapter);
//    }

    /**
     * TODO 某个步骤所需要的材料，目前被取消
     * @param nowlv
     * @return
     */
    private List<Map<String, Object>> geOnetData(ListView nowlv) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        ViewGroup.LayoutParams params = nowlv.getLayoutParams();

        int i = 1;
        for (; i < 5; i++) {
            map = new HashMap<String, Object>();
            map.put("name", "oil " + i);
            map.put("kg", 100 + i);
            list.add(map);
        }
        //动态设置listview的高度
        params.height = ConstHelper.dip2px(RecipeDetailActivity.this, 20 * (i - 1));
        nowlv.setLayoutParams(params);

        return list;
    }

    TextView recipe_dd_likeno_txt;
    int like_count;
    private void coogetThisBookInfo() {
        micocookbook.getCookBookInfo(recipeid, new MiCOCallBack() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, message);
                try {
                    JSONObject datas = new JSONObject(ConstHelper.getFogData(message));
                    JSONObject temp = new JSONObject(datas.getString("Coobook"));

                    String imgurl = temp.getString("mainimageurl");
                    String name = temp.getString("name");
                    String timecount = temp.getString("timecount");
                    like_count = Integer.parseInt(temp.getString("like_count"));
                    String introduction = temp.getString("introduction");
                    itemid = temp.getString("iteamid");

                    TextView recipe_dd_title = (TextView)findViewById(R.id.recipe_dd_title);
                    TextView recipe_dd_cooktime = (TextView)findViewById(R.id.recipe_dd_cooktime);
                    recipe_dd_likeno_txt = (TextView)findViewById(R.id.recipe_dd_likeno_txt);
                    TextView recipe_dd_destxt = (TextView)findViewById(R.id.recipe_dd_destxt);
                    recipe_dd_title.setText(name);
                    recipe_dd_cooktime.setText(timecount + ConstPara.MINUTES);
                    recipe_dd_likeno_txt.setText(like_count+"");
                    recipe_dd_destxt.setText(introduction);

                    JSONArray ingredients = new JSONArray(datas.getString("Items"));
                    initMaterial(ingredients);

                    if(datas.getString("Ilike").equals("1")){
                        recipe_detial_img.setImageResource(R.drawable.recipe_icon_like);
                        recipe_detial_img.setTag(R.drawable.recipe_icon_like);
                    }else{
                        recipe_detial_img.setTag(R.drawable.recipe_icon_favourate_3);
                    }

                    //更新主图
                    SyncImageLoader syncImageLoader = new SyncImageLoader();
                    syncImageLoader.loadImage(HOME_PAGE, null, imgurl, imageLoadListener);

                    //更新步骤
                    JSONArray steps = new JSONArray(datas.getString("Steps"));
                    addLinearLayout(steps);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int code, String message) {
                Log.d(TAG, code + "msg = " + message);
            }
        }, token);
    }

    final int HOME_PAGE = 0;
    SyncImageLoader.OnImageLoadListener imageLoadListener = new SyncImageLoader.OnImageLoadListener() {

        @Override
        public void onImageLoad(final BookModel model, Integer t, final Drawable drawable) {
            ImageView imageView = null;
            switch (t){
                case HOME_PAGE:
                    imageView = (ImageView) findViewById(R.id.recipe_dd_img);
            }
            if (imageView != null) {
                imageView.setBackgroundDrawable(drawable);
            }
        }

        @Override
        public void onError(Integer t) {
        }
    };
}
