package com.example.jessejohnson.abstracttheworld;

import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GetIpAddress extends AsyncTask<String, Integer, String>
{
    @Override
    protected String doInBackground(String... params)
    {
        InetAddress addr = null;
        try
        {
            addr = InetAddress.getLocalHost();
        }

        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        return addr.getHostAddress();
    }

}