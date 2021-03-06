package com.hsk.hxqh.agp_eam.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyco.animation.BaseAnimatorSet;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.flyco.dialog.widget.NormalListDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hsk.hxqh.agp_eam.R;
import com.hsk.hxqh.agp_eam.adpter.BaseQuickAdapter;
import com.hsk.hxqh.agp_eam.adpter.MatrectransAdapter;
import com.hsk.hxqh.agp_eam.api.HttpManager;
import com.hsk.hxqh.agp_eam.api.HttpRequestHandler;
import com.hsk.hxqh.agp_eam.api.JsonUtils;
import com.hsk.hxqh.agp_eam.bean.Results;
import com.hsk.hxqh.agp_eam.config.Constants;
import com.hsk.hxqh.agp_eam.model.MATRECTRANS;
import com.hsk.hxqh.agp_eam.model.PO;
import com.hsk.hxqh.agp_eam.model.WebResult;
import com.hsk.hxqh.agp_eam.ui.activity.option.Matrectrans_chooseActivity;
import com.hsk.hxqh.agp_eam.ui.widget.SwipeRefreshLayout;
import com.hsk.hxqh.agp_eam.unit.AccountUtils;
import com.hsk.hxqh.agp_eam.unit.ArrayUtil;
import com.hsk.hxqh.agp_eam.webserviceclient.AndroidClientService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzw on 2018/6/14.
 */

public class PodetailActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, SwipeRefreshLayout.OnLoadListener {
    public  static  final int POLINE_CODE = 18615;
   LinearLayoutManager layoutManager;
    /**
     * RecyclerView*
     */
    public RecyclerView recyclerView;
    /**
     * 暂无数据*
     */
    private LinearLayout nodatalayout;
    /**
     * 界面刷新*
     */
    private SwipeRefreshLayout refresh_layout = null;
    /**
     * 查询条件*
     */
    private String searchText = "";
    private int page = 1;
    private BaseAnimatorSet mBasIn;
    private BaseAnimatorSet mBasOut;
    private TextView PONUM;//Po
    private TextView DESCRIPTION;//描述
    private TextView STATIONDESC;//占场
    private TextView STATUS;//PO 状态
    private TextView RECEIPTS;// 接收状态
    private TextView ORDERDATE;//订购日期
    private TextView PRETAXTOTAL;//税前总记
    private TextView RECEIVEDTOTALCOST;//已接收成本
    private PO po;
    private ImageView backImag;
    private TextView titleTextView;
    private RelativeLayout  relativeLayout;
    private ArrayList<MATRECTRANS> matrectrans = new ArrayList<>();
    private ArrayList<MATRECTRANS> list = new ArrayList<>();
    private List<MATRECTRANS> listbackup = new ArrayList<>();
    private MatrectransAdapter matrectransAdapter;
    private Button option;
    private Button quit;

    public PodetailActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_po_details);
        getIntendata();
        findViewById();
        initView();
        getData("");
        goodsRecSel();
    }

    @Override
    protected void findViewById() {
        backImag = (ImageView) findViewById(R.id.menu_id);
        titleTextView = (TextView) findViewById(R.id.menu_title);
        PONUM = (TextView) findViewById(R.id.po_ponum_id);
        DESCRIPTION = (TextView) findViewById(R.id.po_desc_id);
        STATIONDESC = (TextView) findViewById(R.id.po_stationdesc_id);
        STATUS = (TextView) findViewById(R.id.po_status_id);
        RECEIPTS = (TextView) findViewById(R.id.po_receipts_id);
        ORDERDATE = (TextView) findViewById(R.id.po_orderdate_id);
        PRETAXTOTAL = (TextView) findViewById(R.id.po_pretaxtotal_id);
        RECEIVEDTOTALCOST = (TextView) findViewById(R.id.po_recivedtotalcost_id);
        relativeLayout = (RelativeLayout) findViewById(R.id.button_layout);
        nodatalayout = (LinearLayout) findViewById(R.id.have_not_data_id);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView_id);
        refresh_layout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        option = (Button) findViewById(R.id.option);
        quit = (Button) findViewById(R.id.back);
    }

    @Override
    protected void initView() {
        relativeLayout.setVisibility(View.VISIBLE);
        backImag.setBackgroundResource(R.drawable.ic_back);
        backImag.setOnClickListener(backOnClickListener);
        titleTextView.setText(R.string.receive);
        PONUM.setText(po.getPONUM());
        DESCRIPTION.setText(po.getDESCRIPTION());
        STATIONDESC.setText(po.getPOSTADES());
        STATUS.setText(po.getSTATUS());
        RECEIPTS.setText(po.getRECEIPTS());
        ORDERDATE.setText(po.getORDERDATE());
        PRETAXTOTAL.setText(po.getPRETAXTOTAL());
        RECEIVEDTOTALCOST.setText(po.getRECEIVEDTOTALCOST());
        layoutManager = new LinearLayoutManager(PodetailActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        refresh_layout.setColor(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        refresh_layout.setRefreshing(true);
        refresh_layout.setOnRefreshListener(this);
        refresh_layout.setOnLoadListener(this);
        option.setOnClickListener(optionOnClickListener);
        quit.setOnClickListener(backOnClickListener);
        initAdapter(new ArrayUtil<MATRECTRANS>());


    }
    public  void getIntendata(){
        Bundle bundle = getIntent().getExtras();
        po = (PO)bundle.get("po");
    }
    private View.OnClickListener backOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    /**
     * 获取数据*
     */
    private void getData(String search) {
        HttpManager.getDataPagingInfo(PodetailActivity.this, HttpManager.getMATRECTRANSUrl(po.getPONUM(),"AGPSITE",1, 20), new HttpRequestHandler<Results>() {
            @Override
            public void onSuccess(Results results) {
                Log.i(TAG, "data=" + results);
            }

            @Override
            public void onSuccess(Results results, int totalPages, int currentPage) {
                ArrayList<MATRECTRANS> item = JsonUtils.parsingMATRECTRANS(PodetailActivity.this, results.getResultlist());
                refresh_layout.setRefreshing(false);
                refresh_layout.setLoading(false);
                if (item == null || item.isEmpty()) {
                    nodatalayout.setVisibility(View.VISIBLE);
                } else {

                    if (item != null || item.size() != 0) {
                            matrectrans = new ArrayList<MATRECTRANS>();
                            initAdapter(item);
                        for (int i = 0; i < item.size(); i++) {
                            matrectrans.add(item.get(i));
                        }
                        addData(item);
                    }
                    nodatalayout.setVisibility(View.GONE);

                    initAdapter(matrectrans);
                }
            }
            @Override
            public void onFailure(String error) {
                refresh_layout.setRefreshing(false);
            }
        });

    }
    public void initAdapter(List<MATRECTRANS> matrectrans){
      matrectransAdapter = new MatrectransAdapter(PodetailActivity.this,R.layout.list_item_invuseline,matrectrans);
        recyclerView.setAdapter(matrectransAdapter);
        matrectransAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener()  {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(PodetailActivity.this, MaterialRetreatingEntity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("matrectrans",matrectransAdapter.getItem(position));
                intent.putExtras(bundle);
                startActivityForResult(intent,position);
            }
        });
    }
    public void addData(List<MATRECTRANS> matrectrans){
        matrectransAdapter.addData(matrectrans);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoad() {

    }
    private View.OnClickListener optionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            option.requestFocus();
            String[] optionlist = {getString(R.string.back),getString(R.string.work_upload),getString(R.string.scan),getString(R.string.reservation),getString(R.string.save)};
            final NormalListDialog normalListDialog = new NormalListDialog(PodetailActivity.this, optionlist);
            normalListDialog.title(getString(R.string.chaxuntj))
                    .showAnim(mBasIn)//
                    .dismissAnim(mBasOut)//
                    .show();
            normalListDialog.setOnOperItemClickL(new OnOperItemClickL() {
                @Override
                public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
//
                    switch (position){
                        case 0://back
                            normalListDialog.superDismiss();
                        finish();
                        case 1:
                            Intent intent1 = new Intent(PodetailActivity.this, WxDemoActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("ownertable", PO.TABLE_NAME);
                            bundle.putString("ownerid",po.getPOID());
                            intent1.putExtras(bundle);
                            startActivity(intent1);
                            break;
                        case 2://
                            normalListDialog.superDismiss();
                            Intent intent = new Intent(PodetailActivity.this,MipcaActivityCapture.class);
                            intent.putExtra("mark", 1); //扫码标识
                            startActivityForResult(intent,POLINE_CODE);
                            break;
                        case 3:
                            normalListDialog.superDismiss();
                            Intent intent2 = new Intent(PodetailActivity.this, Matrectrans_chooseActivity.class);
                            Bundle bundle1 = new Bundle();
                            bundle1.putSerializable("matrectrans",list);
                            intent2.putExtras(bundle1);
                            startActivityForResult(intent2,0);
                            break;
                        case 4:
                            normalListDialog.superDismiss();
                            submitDataInfo();
                            break;
                    }
//                    normalListDialog.dismiss();
                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        nodatalayout.setVisibility(View.GONE);
        switch (requestCode){
            case POLINE_CODE:
                if (data !=null){
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    isExistSN(result);
                }
                break;
        }
        switch (resultCode){
            case 100:
                ArrayList<MATRECTRANS> matrectranslist = (ArrayList<MATRECTRANS>) data.getExtras().get("itemlist");
                if (matrectranslist!=null && !matrectranslist.isEmpty()){
                    for (MATRECTRANS matrectrans:matrectranslist) {
                        matrectrans.setFLAG("I");
                        matrectrans.setPONUM(po.getPONUM());
                        matrectrans.setQUANTITY( matrectrans.getORDERQTYBackup());
                        matrectrans.setRECEIPTQUANTITY( matrectrans.getORDERQTYBackup());
                        matrectrans.setTOSTORELOC(po.getUDSTATION());
                        matrectrans.setENTERBY(AccountUtils.getpersonId(this));
                        matrectrans.setISSUETYPE("RECEIPT");
                        matrectrans.setQTYREQUESTED(matrectrans.getRECEIVEDQTY());
                        matrectransAdapter.add(matrectrans);
                        changegoolsDate();
                    }
                }
                break;
            case 10:
                MATRECTRANS matrectrans1 = (MATRECTRANS) data.getExtras().get("matrectrans");
                matrectransAdapter.remove(requestCode);
                matrectransAdapter.add(requestCode,matrectrans1);
                changegoolsDate();
                break;
        }

    }
    /**
     * 根据SN号查询资产表是否存在
     **/
    private void isExistSN(final String serialnum) {
        boolean flag = false;
        for (MATRECTRANS matrectrans:list) {
            if (matrectrans.getITEMNUM().equals(serialnum.trim()) && Double.parseDouble(matrectrans.getRECEIVEDQTY())>0){
                matrectrans.setFLAG("I");
                matrectrans.setPONUM(po.getPONUM());
                matrectrans.setQUANTITY( matrectrans.getORDERQTYBackup());
                matrectrans.setRECEIPTQUANTITY( matrectrans.getORDERQTYBackup());
                matrectrans.setTOSTORELOC(po.getUDSTATION());
                matrectrans.setENTERBY(AccountUtils.getpersonId(this));
                matrectrans.setISSUETYPE("RECEIPT");
                matrectrans.setQTYREQUESTED(matrectrans.getRECEIVEDQTY());
                matrectransAdapter.add(matrectrans);
                changegoolsDate();
                flag =true;
            }
        }
        if (!flag){
            Toast.makeText(this, getString(R.string.have_not_data_txt),Toast.LENGTH_SHORT).show();
        }
    }

    public void goodsRecSel(){
        new AsyncTask<String,String,String>(){

            @Override
            protected String doInBackground(String... strings) {
                String result = AndroidClientService.goodsRecSel(PodetailActivity.this, po.getPONUM(),po.getUDSTATION(),po.getSTATUS());
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s.startsWith("[")){
                    list = JsonUtils.parsingMATRECTRANS(PodetailActivity.this, s);
                    for (int k= 0;k<list.size();k++){
                        listbackup.add(list.get(k));
                    }
                    changegoolsDate();
                }

            }
        }.execute();

    }
    private void submitDataInfo() {
        final NormalDialog dialog = new NormalDialog(PodetailActivity.this);
        dialog.content(getString(R.string.suretosave)).title(getString(R.string.tip)).btnText(getString(R.string.cancel),getString(R.string.confirm))//
                .showAnim(mBasIn)//
                .dismissAnim(mBasOut)//
                .show();
        dialog.setOnBtnClickL(
                new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                },
                new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                        if (hasEmptyBin()){
                            Toast.makeText(PodetailActivity.this, "ToBin can not be Empty",Toast.LENGTH_LONG).show();
                        }else {
                            showProgressDialog("Waiting...");
                            startAsyncTask();
                        }

                    }
                });
    }
    private void startAsyncTask() {

        new AsyncTask<String, String, WebResult>() {
            @Override
            protected WebResult doInBackground(String... strings) {
                Gson gson = new GsonBuilder().serializeNulls().create();
                String json = "";
                String majson = "";
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("","");
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    jsonObject = new JSONObject(gson.toJson(po));
                    jsonObject.put("relationShip",jsonArray);
                    json = jsonObject.toString();
                    json =json.replace("null","\"\"");
                    majson = gson.toJson(matrectransAdapter.getData());
                    majson = majson.replace("null","\"\"");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                WebResult reviseresult = AndroidClientService.UpdateWO(PodetailActivity.this,json, Constants.PO_NAME,"PONUM", po.getPONUM(),Constants.WORK_URL);
                String stringrel = AndroidClientService.insertMa(PodetailActivity.this, majson);
                String rest = "";
                if (stringrel!=null){
                    String[] results = stringrel.split("!");
                    for (int i = 0;i<results.length;i++){
                        if (results[i].contains("succeed")){
                            rest = "成功";
                            continue;
                        }else {
                            rest = "The" + i + "Failed";
                            break;
                        }
                    }
                    reviseresult.errorMsg = rest;
                }else {
                    reviseresult.errorMsg = "No Records";
                }
                return reviseresult;
            }

            @Override
            protected void onPostExecute(WebResult workResult) {
                super.onPostExecute(workResult);
                if (workResult.errorMsg.equals("成功")) {
                    Toast.makeText(PodetailActivity.this,getString(R.string.success), Toast.LENGTH_LONG).show();
                    for (int i = 0;i < matrectransAdapter.getItemCount();i++){
                        matrectransAdapter.getItem(i).setFLAG("U");
                    }
                } else {
                    Toast.makeText(PodetailActivity.this, workResult.errorMsg, Toast.LENGTH_LONG).show();
                }
                closeProgressDialog();
            }
        }.execute();
    }
    public void changegoolsDate(){
        if(matrectransAdapter.getItemCount()>0){
            int size = listbackup.size();
            for (int i = 0;i< size ;i++){
                String polinenum = listbackup.get(i).getPOLINENUM();
                double qty = 0;
                for (int j=0;j<matrectransAdapter.getItemCount();j++) {
                    if (polinenum.equals(matrectransAdapter.getItem(j).getPOLINENUM())){
                        if("".equals(matrectransAdapter.getItem(j).getRECEIPTQUANTITY())){
                            qty +=0;
                        } else {
                            qty +=Double.parseDouble(matrectransAdapter.getItem(j).getRECEIPTQUANTITY());
                        }

                    }
                }
                double orderqty = Double.parseDouble(listbackup.get(i).getORDERQTY());
                    for (int k = 0;k<list.size();k++){
                        if (polinenum.equals(list.get(k).getPOLINENUM())){
                            double back = orderqty-qty;
                                list.get(k).setORDERQTYBackup(back+"");
                                list.get(k).setRECEIVEDQTY(back+"");
                            break;
                        }
                    }
            }
        }

    }
    public boolean hasEmptyBin(){
        boolean flag = false;
        for (int i= 0 ;i<matrectransAdapter.getItemCount();i++){
            String toBin = matrectransAdapter.getItem(i).getTOBIN();
            if (toBin == null || "".equalsIgnoreCase(toBin)){
                flag = true;
                break;
            }
        }
        return flag;
    }
}


