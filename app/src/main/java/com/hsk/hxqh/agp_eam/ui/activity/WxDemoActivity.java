package com.hsk.hxqh.agp_eam.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hsk.hxqh.agp_eam.R;
import com.hsk.hxqh.agp_eam.adpter.ImagePickerAdapter;
import com.hsk.hxqh.agp_eam.config.Constants;
import com.hsk.hxqh.agp_eam.ui.widget.GlideImageLoader;
import com.hsk.hxqh.agp_eam.unit.ImageCompressUtils;
import com.hsk.hxqh.agp_eam.unit.MessageUtils;
import com.hsk.hxqh.agp_eam.webserviceclient.AndroidClientService;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.ui.ImagePreviewDelActivity;
import com.lzy.imagepicker.view.CropImageView;

import org.kobjects.base64.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


public class WxDemoActivity extends BaseActivity implements ImagePickerAdapter.OnRecyclerViewItemClickListener {

    private static final String TAG = "WxDemoActivity";
    public static final int IMAGE_ITEM_ADD = -1;
    public static final int REQUEST_CODE_SELECT = 100;
    public static final int REQUEST_CODE_PREVIEW = 101;

    private ImagePickerAdapter adapter;
    private ArrayList<ImageItem> selImageList = new ArrayList<>();
    ; //当前选择的所有图片
    private int maxImgCount = 9;               //允许选择图片最大数

    /**
     * 返回按钮
     */
    private ImageView backImageView;
    /**
     * 标题
     */
    private TextView titleTextView;
    /**
     * text_hint*
     */
    private TextView textView;
    /**
     * 提交按钮*
     */
    private Button submitBtn;

    /**
     * 选择图片*
     */
    private Button chooseImageBtn;


    private int iscamera;


    ArrayList<ImageItem> imageItems;

    /**
     * 表名*
     */
    private String ownertable;
    /**
     * 主键ID*
     */
    private String ownerid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        getInitData();
        findViewById();
        initView();
        initWidget();
        initImagePicker();
    }

    private void getInitData() {
        iscamera = getIntent().getExtras().getInt("isCamera");
        if (iscamera == 1) {
            ImagePicker.getInstance().setSelectLimit(maxImgCount - selImageList.size());
            Intent intent = new Intent(this, ImageGridActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SELECT);
        }

        ownertable = getIntent().getExtras().getString("ownertable");
        ownerid = getIntent().getExtras().getString("ownerid");
    }

    @Override
    protected void findViewById() {
        backImageView = (ImageView) findViewById(R.id.menu_id);
        titleTextView = (TextView) findViewById(R.id.menu_title);
        textView = (TextView) findViewById(R.id.text_id);
        submitBtn = (Button) findViewById(R.id.submit_btn);
        chooseImageBtn = (Button) findViewById(R.id.picker_btn);
    }

    @Override
    protected void initView() {
        backImageView.setBackgroundResource(R.drawable.ic_back);
        backImageView.setOnClickListener(backImageViewOnClickListener);
        titleTextView.setText(getString(R.string.work_commit));
        chooseImageBtn.setOnClickListener(chooseImageBtnOnClickListener);
        submitBtn.setVisibility(View.VISIBLE);
        submitBtn.setOnClickListener(submitBtnOnClickListener);

    }

    private View.OnClickListener backImageViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };


    private void initWidget() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        selImageList = new ArrayList<>();
        adapter = new ImagePickerAdapter(this, selImageList, maxImgCount);
        adapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);                      //显示拍照按钮
        imagePicker.setCrop(false);                           //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true);                   //是否按矩形区域保存
        imagePicker.setSelectLimit(maxImgCount);              //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);                       //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);                      //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);                         //保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);                         //保存文件的高度。单位像素
    }


    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
//            case IMAGE_ITEM_ADD:
//                //打开选择,本次允许选择的数量
//                ImagePicker.getInstance().setSelectLimit(maxImgCount - selImageList.size());
//                Intent intent = new Intent(this, ImageGridActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_SELECT);
//                break;
            default:
                //打开预览
                Intent intentPreview = new Intent(this, ImagePreviewDelActivity.class);
                intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, (ArrayList<ImageItem>) adapter.getImages());
                intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
                intentPreview.putExtra("results", ImagePicker.LOCATION_IMAGE_ITEMS);
                startActivityForResult(intentPreview, REQUEST_CODE_PREVIEW);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "resultCode=" + resultCode + "requestCode=" + requestCode);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //添加图片返回
            if (data != null && requestCode == REQUEST_CODE_SELECT) {
                imageItems = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                selImageList.addAll(imageItems);
                adapter.setImages(selImageList);
                if (adapter.getItemCount() != 0) {
                    textView.setVisibility(View.GONE);
                } else {
                    textView.setVisibility(View.VISIBLE);
                }

            }
        } else if (resultCode == ImagePicker.RESULT_CODE_BACK) {
            //预览图片返回
            if (data != null && requestCode == REQUEST_CODE_PREVIEW) {
                imageItems = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);

                selImageList.clear();
                selImageList.addAll(imageItems);
                adapter.setImages(selImageList);
                adapter.notifyDataSetChanged();
                if (adapter.getItemCount() != 0) {
                    textView.setVisibility(View.GONE);
                } else {
                    textView.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    /**
     * 选择照片的事件
     **/
    private View.OnClickListener chooseImageBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //打开选择,本次允许选择的数量
            ImagePicker.getInstance().setSelectLimit(maxImgCount - selImageList.size());
            Intent intent = new Intent(WxDemoActivity.this, ImageGridActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SELECT);
        }
    };

    /**
     * 上传
     **/
    private View.OnClickListener submitBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Log.i(TAG, "imageItems=" + imageItems.size());
            if (imageItems == null || imageItems.size() == 0) {
                MessageUtils.showMiddleToast(WxDemoActivity.this, getString(R.string.check_photo_hint));
            } else {
                showProgressDialog(".....");
                progressDialog.setCancelable(false);
                for (int i = 0; i < imageItems.size(); i++) {
                    startAsyncTask(imageItems.get(i).path);
                }
            }
        }
    };


    /**
     * 测试图片上传*
     */
    private void startAsyncTask(String fileName) {
        File f = new File(fileName);

        ImageCompressUtils.from(WxDemoActivity.this)
                .load(fileName)
                .execute(new ImageCompressUtils.OnCompressListener() {
                    @Override
                    public String onSuccess(File file) {

                        try {
                            FileInputStream fis = new FileInputStream(file);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int count = 0;
                            while ((count = fis.read(buffer)) >= 0) {
                                baos.write(buffer, 0, count);
                            }
                            String uploadBuffer = new String(Base64.encode(baos.toByteArray()));  //进行Base64编码
                            fis.close();


                            String name = getFileName(file.getPath());

                            final String finalUpdataInfo = uploadBuffer;
                            final String finalname = name;
                            new AsyncTask<String, String, String>() {
                                @Override
                                protected String doInBackground(String... strings) {
                                    String reviseresult = AndroidClientService.connectWebService(WxDemoActivity.this,
                                            finalname, finalUpdataInfo, ownertable, ownerid, Constants.WORK_URL);
                                    return reviseresult;
                                }

                                @Override
                                protected void onPostExecute(String workResult) {
                                    super.onPostExecute(workResult);
                                    if (workResult == null) {
                                        MessageUtils.showMiddleToast(WxDemoActivity.this, getString(R.string.fail));
                                    } else {
                                        MessageUtils.showMiddleToast(WxDemoActivity.this, getString(R.string.success));
                                        finish();
                                    }
                                    closeProgressDialog();
                                }
                            }.execute();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    ;

                    @Override
                    public void onError(Throwable e) {
                        MessageUtils.showMiddleToast(WxDemoActivity.this, getString(R.string.fail));
                    }
                });

    }

//    /**
//     * 提交数据*
//     */
//    private void startAsyncTask(String fileName) {
//        String updataInfo = null;
//        String name = null;
//        updataInfo = getuploadBuffer(fileName);
//
//    }


    /**
     * 获取文件名称*
     */
    private String getFileName(String fileName) {
        File file = new File(fileName);
        String name = null;
        if (file.exists()) {
            name = file.getName();
        }
        return name;
    }


}
