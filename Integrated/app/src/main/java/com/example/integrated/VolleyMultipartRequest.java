package com.example.integrated;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class VolleyMultipartRequest extends Request<String> {
    private final Response.Listener<String> mListener;
    private final File file;
    private final String word;
    private static final String LINE_END = "\r\n";
    private static final String PREFIX = "--";
    private static final String BOUNDARY = "apiclient-" + System.currentTimeMillis();

    public VolleyMultipartRequest(String url, String word, File file,
                                  Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.file = file;
        this.word = word;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + BOUNDARY;
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            // Populate text payload
            dos.writeBytes(PREFIX + BOUNDARY + LINE_END);
            dos.writeBytes("Content-Disposition: form-data; name=\"word\"" + LINE_END);
            dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + LINE_END);
            dos.writeBytes(LINE_END);
            dos.writeBytes(this.word + LINE_END);
            dos.flush();

            // Populate file payload
            dos.writeBytes(PREFIX + BOUNDARY + LINE_END);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + LINE_END);
            dos.writeBytes("Content-Type: application/octet-stream" + LINE_END);
            dos.writeBytes(LINE_END);

            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            fileInputStream.close();
            dos.writeBytes(LINE_END);

            // End of multipart/form-data.
            dos.writeBytes(PREFIX + BOUNDARY + PREFIX + LINE_END);
            dos.flush();
            dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success(new String(response.data), HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
    }
}

