package utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.provider.Settings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/7/2 0002.
 */

public class EthernetUtils {
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

    @SuppressLint("WrongConstant")
    public EthernetUtils(Context mContext) {
        this.mContext = mContext;
        if (ethernetManager == null) {
            ethernetManager = (EthernetManager) mContext.getSystemService(ETHERNET_SERVICE);
            contentResolver = mContext.getContentResolver();
        }
    }


    //动态IP获取
    @SuppressLint("NewApi")
    public void DhcpEthernet() {
        Settings.System.putInt(contentResolver, ETHERNET_USE_STATIC_IP, 0);
        mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, null, ProxyInfo.buildDirectProxy(null, 0));
        ethernetManager.setConfiguration(mIpConfiguration);
        mStaticIpConfiguration = new StaticIpConfiguration();
    }


    public void StaticEthernet(String ipAddress, String ipMask, String gateway, String dns1, String dns2) {
        Settings.System.getInt(contentResolver, ETHERNET_USE_STATIC_IP, 1);

        if (isIpAddress(ipAddress)) {
            Inet4Address iPv4Address = getIPv4Address(ipAddress);
            int mIpMask = maskStr2InetMask(ipMask);
            InetAddress mGateway = getIPv4Address(gateway);
            InetAddress mDns1 = getIPv4Address(dns1);

            Class<?> clazz = null;

            try {
                clazz = Class.forName("android.net.LinkAddress");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Class[] cl = new Class[]{InetAddress.class, int.class};
            Constructor cons = null;
            try {
                assert clazz != null;
                cons = clazz.getConstructor(cl);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            Object[] x = {iPv4Address, mIpMask};
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
            mStaticIpConfiguration.gateway = mGateway;
            mStaticIpConfiguration.dnsServers.add(mDns1);
            if (!dnsStr2.isEmpty()) {
                mStaticIpConfiguration.dnsServers.add(EthernetUtils.getIPv4Address(dnsStr2));
            }
            mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, mStaticIpConfiguration, null);
            ethernetManager.setConfiguration(mIpConfiguration);
            //put到系统数据库
            Settings.System.putString(contentResolver, ETHERNET_STATIC_IP, ipAddress);
            Settings.System.putString(contentResolver, ETHERNET_STATIC_NETMASK, ipMask);
            Settings.System.putString(contentResolver, ETHERNET_STATIC_GATEWAY, gateway);
            Settings.System.putString(contentResolver, ETHERNET_STATIC_DNS1, dns1);
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


    public static Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }


    public static int maskStr2InetMask(String maskStr) {
        StringBuffer sb;
        String str;
        int inetmask = 0;
        int count = 0;
        /*
         * check the subMask format
         */
        Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
        if (!pattern.matcher(maskStr).matches()) {
            return 0;
        }
        String[] ipSegment = maskStr.split("\\.");
        for (int n = 0; n < ipSegment.length; n++) {
            sb = new StringBuffer(Integer.toBinaryString(Integer.parseInt(ipSegment[n])));
            str = sb.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf("1", i);
                if (i == -1)
                    break;
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }


}
