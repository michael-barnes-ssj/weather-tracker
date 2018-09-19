package bit.mike.googlemaps;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by Mike on 6/06/2018.
 * Gets image from weather api service and sets image
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    private ImageView imageView;

    public DownloadImageTask(ImageView imageView)
    {
        this.imageView = imageView;
    }

    /**
     *
     * @param urls Uses url to get image and decodes it
     * @return
     */
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap bmp = null;
        try
        {
            InputStream in = new java.net.URL(urldisplay).openStream();
            bmp = BitmapFactory.decodeStream(in);
        } catch (Exception e) {  }
        return bmp;
    }

    /**
     * When image has been fetched, set image view bitmap
     * @param result
     */
    protected void onPostExecute(Bitmap result) {
        imageView.setImageBitmap(result);
    }
}
