package com.module.ethernet;

import android.annotation.SuppressLint;
import android.os.SystemProperties;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.module.ethernet.utils.IPUtil;
import com.module.ethernet.utils.NetUtil;
import com.module.ethernet.utils.NetWorkType;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button dhcp_button, static_button, save_static_button;
    private EthernetMain ethernetMain;
    private EditText etIp;
    private EditText et_mask;
    private EditText etIpWg;
    private EditText etDns1;
    private EditText etDns2;
    private TextView tvIpContent, tvIpmask, tvIpgateway, tvIpDns1, tvIpDns2;
    private LinearLayout ll_dhcp;
    private GridLayout gl_static;
    private String ipaddress;
    private String ipmask;
    private String gateway;
    private String dns01;
    private String dns02;
    private String ip;
    private String zwym;
    private String ipwg;
    private String dns1;
    private String dns2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dhcp_button = (Button) findViewById(R.id.dhcp_button);
        static_button = (Button) findViewById(R.id.static_button);
        save_static_button = (Button) findViewById(R.id.save_static_button);

        ethernetMain = new EthernetMain(this);
        initView();
        initData();
        dhcp_button.setOnClickListener(this);
        static_button.setOnClickListener(this);
        save_static_button.setOnClickListener(this);
        ll_dhcp.setVisibility(View.VISIBLE);
        etIp.setText(ipaddress);
        etIpWg.setText(gateway);
        et_mask.setText(ipmask);
        etDns1.setText(dns01);
        etDns2.setText(dns02);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dhcp_button:
                ethernetMain.dhcpEth();
                gl_static.setVisibility(View.GONE);
                ll_dhcp.setVisibility(View.VISIBLE);
                save_static_button.setVisibility(View.GONE);
                break;
            case R.id.static_button:
                /**
                 * @param ip IP地址
                 * @param fix 子网掩码
                 * @param dns1 DNS1
                 * @param dns2 DNS2
                 * @param gw 默认网关
                 */
                ll_dhcp.setVisibility(View.GONE);
                gl_static.setVisibility(View.VISIBLE);
                save_static_button.setVisibility(View.VISIBLE);
                break;
            case R.id.save_static_button:
                setStaticIP();
                setFocus();
                Toast.makeText(this, "静态IP保存成功", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void setFocus() {
        etIp.clearFocus();
        et_mask.clearFocus();
        etIpWg.clearFocus();
        etDns1.clearFocus();
        etDns2.clearFocus();
    }

    private void setStaticIP() {
//                ethernetMain.staticEth("192.168.1.222","255.255.255.0","192.168.1.1","192.168.1.1","192.168.1.1");
        ip = etIp.getText().toString();
        zwym = et_mask.getText().toString();
        ipwg = etIpWg.getText().toString();
        dns1 = etDns1.getText().toString();
        dns2 = etDns2.getText().toString();
        ethernetMain.staticEth(ip, zwym, dns1, dns2, ipwg);

    }


    private void initView() {
        etIp = (EditText) findViewById(R.id.et_ip);
        et_mask = (EditText) findViewById(R.id.et_mask);
        etIpWg = (EditText) findViewById(R.id.et_ip_gateway);
        etDns1 = (EditText) findViewById(R.id.et_dns1);
        etDns2 = (EditText) findViewById(R.id.et_dns2);

        ll_dhcp = (LinearLayout) findViewById(R.id.ll_ip_dhcp);
        gl_static = (GridLayout) findViewById(R.id.gl_static_ip);

        tvIpContent = (TextView) findViewById(R.id.tv_ip_address);
        tvIpmask = (TextView) findViewById(R.id.tv_ip_mask);
        tvIpgateway = (TextView) findViewById(R.id.tv_gateway);
        tvIpDns1 = (TextView) findViewById(R.id.tv_ip_Dns1);
        tvIpDns2 = (TextView) findViewById(R.id.tv_ip_Dns2);
    }

    private void initData() {

        ipaddress = SystemProperties.get("dhcp.eth0.ipaddress");
        ipmask = SystemProperties.get("dhcp.eth0.mask");
        gateway = SystemProperties.get("dhcp.eth0.gateway");
        dns01 = SystemProperties.get("dhcp.eth0.dns1");
        dns02 = SystemProperties.get("dhcp.eth0.dns2");
        Observable.just("one")
                .map(new Function<String, NetWorkType>() {
                    @Override
                    public NetWorkType apply(String s) throws Exception {
                        NetWorkType netWorkType = IPUtil.getNetWorkType();
                        return netWorkType;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<NetWorkType>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void accept(NetWorkType s) throws Exception {
                        tvIpContent.setText("IP地址:      " + ipaddress);
                        tvIpmask.setText("子网掩码:     " + ipmask);
                        tvIpgateway.setText("网关:        " + gateway);
                        tvIpDns1.setText("DNS1:     " + dns01);
                        tvIpDns2.setText("DNS2:     " + dns02);
                    }
                });
    }
}
