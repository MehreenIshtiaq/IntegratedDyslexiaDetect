package com.example.integrated;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MultipartRequest extends Request<String> {
    private final Response.Listener<String> mListener;
    private final Response.ErrorListener mErrorListener;
    private final String mBoundary = "apiclient-" + System.currentTimeMillis();
    private final String mMimeType = "multipart/form-data;boundary=" + mBoundary;
    private ByteArrayOutputStream mOutputStream = new ByteArrayOutputStream();
    private DataOutputStream mDataOutputStream = new DataOutputStream(mOutputStream);

    public MultipartRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    public void addFilePart(String fieldName, String fileName, byte[] fileData, String type) throws IOException {
        mDataOutputStream.writeBytes("--" + mBoundary + "\r\n");
        mDataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        mDataOutputStream.writeBytes("Content-Type: " + type + "\r\n");
        mDataOutputStream.writeBytes("\r\n");
        mDataOutputStream.write(fileData);
        mDataOutputStream.writeBytes("\r\n");
    }

    public void addFormField(String name, String value) throws IOException {
        mDataOutputStream.writeBytes("--" + mBoundary + "\r\n");
        mDataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
        mDataOutputStream.writeBytes("\r\n");
        mDataOutputStream.writeBytes(value);
        mDataOutputStream.writeBytes("\r\n");
    }

    @Override
    public String getBodyContentType() {
        return mMimeType;
    }

    @Override
    public byte[] getBody() {
        try {
            mDataOutputStream.writeBytes("--" + mBoundary + "--\r\n");
            return mOutputStream.toByteArray();
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return null;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success("Uploaded", HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
}

