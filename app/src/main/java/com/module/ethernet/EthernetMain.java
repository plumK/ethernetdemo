package com.module.ethernet;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.module.ethernet.utils.EthernetUtils;

import java.io.FileDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;


/**
 * @author Crystal lee
 * @package com.module.ethernet
 * @fileName EthernetMain
 * @date on 2018/6/28 0028
 * @describe TODO
 */

public class EthernetMain {
    private static final String ETHERNET_USE_STATIC_IP = "ethernet_use_static_ip";
    private static final String ETHERNET_STATIC_IP = "ethernet_static_ip";
    private static final String ETHERNET_STATIC_GATEWAY = "ethernet_static_gateway";
    private static final String ETHERNET_STATIC_NETMASK = "ethernet_static_netmask";
    private static final String ETHERNET_STATIC_DNS1 = "ethernet_static_dns1";
    public static final String ETHERNET_STATIC_DNS2 = "ethernet_static_dns2";

    private static final String ETHERNET_SERVICE = "ethernet";
    private EthernetManager ethernetManager;
    private Context mContext;
    private ContentResolver contentResolver;
    private StaticIpConfiguration mStaticIpConfiguration;
    private IpConfiguration mIpConfiguration;
    private Inet4Address inetAddr;
    private int prefixLength;
    private InetAddress gatewayAddr;
    private InetAddress dnsAddr;

    @SuppressLint("WrongConstant")
    public EthernetMain(Context context) {
        this.mContext = context;
        if (ethernetManager == null) {
            ethernetManager = (EthernetManager) mContext.getSystemService(ETHERNET_SERVICE);
        }
        init();
    }

    private void init() {
        contentResolver = mContext.getContentResolver();
    }


    //设置为动态ip
    public void dhcpEth() {
        Settings.System.putInt(contentResolver, ETHERNET_USE_STATIC_IP, 0);
        mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, null, ProxyInfo.buildDirectProxy(null, 0));
        ethernetManager.setConfiguration(mIpConfiguration);
    }

    //设置为静态IP

    /**
     * @param ip   IP地址
     * @param fix  子网掩码
     * @param dns1 DNS1
     * @param dns2 DNS2
     * @param gw   默认网关
     */

    public void staticEth(String ip, String fix, String dns1, String dns2, String gw) {
        Settings.System.getInt(contentResolver, ETHERNET_USE_STATIC_IP, 1);
        //如果输入的IP地址格式正确
        if (isIpAddress(ip)) {
            mStaticIpConfiguration = new StaticIpConfiguration();
            prefixLength = EthernetUtils.maskStr2InetMask(fix);
            inetAddr = EthernetUtils.getIPv4Address(ip);
            gatewayAddr = EthernetUtils.getIPv4Address(gw);
            dnsAddr = EthernetUtils.getIPv4Address(dns1);
            Class<?> clazz = null;

            try {
                clazz = Class.forName("android.net.LinkAddress");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Class[] cl = new Class[]{InetAddress.class, int.class};
            Constructor cons = null;
            try {
                cons = clazz.getConstructor(cl);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            Object[] x = {inetAddr, prefixLength};
            String dnsStr2 = dns2;

            try {
                assert cons != null;
                mStaticIpConfiguration.ipAddress = (LinkAddress) cons.newInstance(x);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            mStaticIpConfiguration.gateway = gatewayAddr;
            mStaticIpConfiguration.dnsServers.add(dnsAddr);
            if (!dnsStr2.isEmpty()) {
                mStaticIpConfiguration.dnsServers.add(EthernetUtils.getIPv4Address(dnsStr2));
            }
            mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, mStaticIpConfiguration, null);
            ethernetManager.setConfiguration(mIpConfiguration);

            //put到系统数据库
            Settings.System.putString(contentResolver, ETHERNET_STATIC_IP, ip);
            Settings.System.putString(contentResolver, ETHERNET_STATIC_NETMASK, fix);
            Settings.System.putString(contentResolver, ETHERNET_STATIC_DNS1, dns1);
            Settings.System.putString(contentResolver, ETHERNET_STATIC_GATEWAY, gw);
        }
    }


    //判断ip地址的正确性
    private boolean isIpAddress(String value) {
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;

        while (start < value.length()) {
            if (end == -1) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

            numBlocks++;

            start = end + 1;
            end = value.indexOf('.', start);
        }
        return numBlocks == 4;
    }


}
