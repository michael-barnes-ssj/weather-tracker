package bit.mike.googlemaps;

import android.content.res.Resources;
import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Mike on 21/05/2018.
 * Gets JSON from url and returns it as a string
 */

public class HTTPRequest
{
    public static String getJSONString(String url, Resources r)
    {
        final int CONNECTION_SUCCESS = 200;
        String JSONString = null;

        try {
            //Make url object
            URL URLObject = new URL(url);
            //Create connection
            HttpURLConnection connection = (HttpURLConnection) URLObject.openConnection();
            connection.connect();
            connection.setRequestMethod(r.getString(R.string.get));

            int response = connection.getResponseCode();

            if (response == CONNECTION_SUCCESS) //Success
            {
                //Set up for reading
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                BufferedReader br = new BufferedReader(isr);
                String responseString;
                StringBuilder sb = new StringBuilder();

                while ((responseString = br.readLine()) != null)
                {
                    sb = sb.append(responseString);
                }

                JSONString = sb.toString();
            }
        }
        catch (MalformedURLException e)
        {
            Log.d(r.getString(R.string.error_malformed), e.getMessage());
        }
        catch (IOException e)
        {
            Log.d(r.getString(R.string.error_io), e.getMessage());
        }
        catch (IllegalStateException e)
        {
            Log.d(r.getString(R.string.error_illegalstate),  e.getMessage());
        }

        return JSONString;
    }
}

