package vn.duytruong.appbandienthoai.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.DonHangAdapter;
import vn.duytruong.appbandienthoai.model.DonHang;
import vn.duytruong.appbandienthoai.utils.Utils;

public class DonHangActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DonHangAdapter adapter;
    private List<DonHang> donHangList;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_don_hang);

        initView();
        initControl();
        getDonHang();
    }

    private void initControl() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Đơn hàng của tôi");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        requestQueue = Volley.newRequestQueue(this);
        donHangList = new ArrayList<>();
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Pass isAdmin flag so adapter can choose admin layout when admin is logged in
        boolean isAdmin = (Utils.user_current != null && Utils.user_current.isAdmin());
        adapter = new DonHangAdapter(this, donHangList, isAdmin);
        recyclerView.setAdapter(adapter);
    }

    private void getDonHang() {
        // Nếu user hiện tại là admin thì yêu cầu server trả về tất cả đơn của user role=0
        String url;
        if (Utils.user_current != null && Utils.user_current.isAdmin()) {
            // include admin id so server can verify the requester is actually an admin
            url = Utils.URL_GET_DON_HANG + "?isadmin=1&iduser=" + Utils.user_current.getId();
        } else {
            url = Utils.URL_GET_DON_HANG + "?iduser=" + Utils.user_current.getId();
        }

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                
                                donHangList.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject item = jsonArray.getJSONObject(i);
                                    
                                    DonHang donHang = new DonHang();
                                    donHang.setId(item.getInt("id"));
                                    donHang.setMadonhang(item.getString("madonhang"));
                                    donHang.setDiachi(item.getString("diachi"));
                                    donHang.setSodienthoai(item.getString("sodienthoai"));
                                    donHang.setSoluong(item.getInt("soluong"));
                                    donHang.setTongtien(item.getString("tongtien"));
                                    donHang.setNgaydat(item.getString("ngaydat"));
                                    donHang.setNgaygiaodukien(item.getString("ngaygiaodukien"));
                                    // Normalize trangthai to human-readable label (handles numeric codes from server)
                                    donHang.setTrangthai(Utils.formatTrangThai(item.getString("trangthai")));

                                    donHangList.add(donHang);
                                }
                                
                                adapter.notifyDataSetChanged();
                                
                                if (donHangList.size() == 0) {
                                    Toast.makeText(DonHangActivity.this, 
                                            "Bạn chưa có đơn hàng nào", 
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(DonHangActivity.this, 
                                        jsonObject.getString("message"), 
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(DonHangActivity.this, 
                                    "Lỗi xử lý dữ liệu", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(DonHangActivity.this, 
                                "Lỗi kết nối", 
                                Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(request);
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewDonHang);
    }
}
